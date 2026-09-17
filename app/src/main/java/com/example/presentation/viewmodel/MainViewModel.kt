package com.example.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.Coupon
import com.example.data.model.Emotion
import com.example.data.model.Event
import com.example.data.model.Item
import com.example.data.model.Life
import com.example.data.model.NPC
import com.example.data.model.OCEAN
import com.example.data.model.Quest
import com.example.data.model.Relationship
import com.example.data.model.Stats
import com.example.data.model.SubscriptionRequest
import com.example.data.model.SubscriptionStatus
import com.example.data.model.WorldState
import com.example.data.remote.PuterAiModel
import com.example.data.remote.PuterBridge
import com.example.data.remote.PuterUserInfo
import com.example.data.repository.AiRepository
import com.example.data.repository.BillingRepository
import com.example.data.repository.LifeRepository
import com.example.domain.ai.CoordinationHub
import com.example.domain.ai.MacroStateDirector
import com.example.domain.pdf.LifeStoryPdfExporter
import com.example.domain.simulation.CareerEconomyEngine
import com.example.domain.simulation.CountryData
import com.example.domain.simulation.LegacyAssessment
import com.example.domain.simulation.LifestyleSocialEngine
import com.example.domain.simulation.NarrativeQuestEngine
import com.example.domain.simulation.WorldGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import kotlin.random.Random

data class MainUiState(
    val activeLife: Life? = null,
    val allSavedLives: List<Life> = emptyList(),
    val npcs: List<NPC> = emptyList(),
    val worldState: WorldState = WorldState(),
    val recentEvents: List<Event> = emptyList(),
    val activeQuests: List<Quest> = emptyList(),
    val userPreferences: UserPreferences? = null,
    val isAiThinking: Boolean = false,
    val currentDialogueNpc: NPC? = null,
    val dialogueHistory: List<Pair<String, String>> = emptyList(), // Pair(Speaker, Text)
    val legacyAssessment: LegacyAssessment? = null,
    val toastMessage: String? = null,
    val subscriptionRequests: List<SubscriptionRequest> = emptyList(),
    val puterUser: PuterUserInfo = PuterUserInfo(),
    val availableAiModels: List<PuterAiModel> = PuterBridge.defaultModelCatalog(),
    val isPuterAuthenticating: Boolean = false
)

class MainViewModel(
    private val lifeRepository: LifeRepository,
    private val billingRepository: BillingRepository,
    private val aiRepository: AiRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val puterBridge: PuterBridge
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeActiveLife()
        observeAllLives()
        observeNpcs()
        observePreferences()
        observeSubscriptionRequests()
        observePuterState()
    }

    private fun observePuterState() {
        viewModelScope.launch {
            puterBridge.authStateFlow.collect { user ->
                _uiState.value = _uiState.value.copy(puterUser = user)
            }
        }
        viewModelScope.launch {
            puterBridge.availableModelsFlow.collect { models ->
                if (models.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(availableAiModels = models)
                }
            }
        }
    }

    fun authenticateWithPuter(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPuterAuthenticating = true)
            val result = puterBridge.authSignIn()
            _uiState.value = _uiState.value.copy(isPuterAuthenticating = false)
            result.onSuccess { user ->
                showToast("Connected to Puter.js Cloud • ${user.username ?: "Citizen"}")
                onComplete?.invoke()
            }.onFailure { err ->
                showToast("Puter Auth: ${err.message ?: "Completed"}")
                onComplete?.invoke()
            }
        }
    }

    fun signOutPuter() {
        viewModelScope.launch {
            puterBridge.authSignOut()
            showToast("Signed out of Puter.js")
        }
    }

    fun refreshAvailableAiModels() {
        viewModelScope.launch {
            val models = puterBridge.listModels()
            _uiState.value = _uiState.value.copy(availableAiModels = models)
            showToast("Loaded ${models.size} AI models from Puter.js")
        }
    }

    private fun observeSubscriptionRequests() {
        viewModelScope.launch {
            billingRepository.subscriptionRequestsFlow.collect { requests ->
                _uiState.value = _uiState.value.copy(subscriptionRequests = requests)
            }
        }
    }

    private fun observeActiveLife() {
        viewModelScope.launch {
            lifeRepository.activeLifeFlow.collect { life ->
                _uiState.value = _uiState.value.copy(activeLife = life)
                if (life != null) {
                    lifeRepository.getEventsFlow(life.id).collect { evts ->
                        _uiState.value = _uiState.value.copy(recentEvents = evts)
                    }
                }
            }
        }
    }

    private fun observeAllLives() {
        viewModelScope.launch {
            lifeRepository.allLivesFlow.collect { lives ->
                _uiState.value = _uiState.value.copy(allSavedLives = lives)
            }
        }
    }

    private fun observeNpcs() {
        viewModelScope.launch {
            lifeRepository.allNpcsFlow.collect { npcs ->
                _uiState.value = _uiState.value.copy(npcs = npcs)
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(userPreferences = prefs)
            }
        }
    }

    // GAME ACTIONS
    fun startNewLife(
        name: String,
        gender: String,
        country: CountryData,
        traits: List<String>,
        difficulty: String,
        sandbox: Boolean,
        challenge: Boolean
    ) {
        viewModelScope.launch {
            val lifeId = UUID.randomUUID().toString()
            val initialMoney = if (sandbox) 100_000.0 else 500.0

            val initialStats = Stats(
                hunger = 100f,
                social = 100f,
                hygiene = 100f,
                funLevel = 100f,
                health = 100f,
                fitness = if (traits.contains("Athletic")) 80f else 50f,
                intellect = if (traits.contains("Genius")) 85f else 50f,
                looks = if (traits.contains("Attractive")) 80f else 50f,
                happiness = 85f,
                karma = 50f
            )

            val initialLife = Life(
                id = lifeId,
                playerName = name,
                age = 18,
                country = country.name,
                traits = traits,
                stats = initialStats,
                money = initialMoney,
                career = null,
                relationships = emptyList(),
                inventory = emptyList(),
                isActive = true,
                createdAt = System.currentTimeMillis()
            )

            lifeRepository.saveLife(initialLife)
            lifeRepository.switchToLife(lifeId)
            userPreferencesRepository.setActiveLifeId(lifeId)

            lifeRepository.logEvent(
                lifeId = lifeId,
                type = "Life Begins",
                description = "You began your journey as $name in ${country.name}."
            )

            // Seed initial local NPCs
            val initialNpcs = listOf(
                NPC(
                    id = "npc_${UUID.randomUUID().toString().take(6)}",
                    name = "Elena Rostova",
                    age = 22,
                    gender = "Female",
                    ocean = OCEAN(0.7f, 0.6f, 0.8f, 0.9f, 0.2f),
                    emotion = Emotion(0.4f, 0.6f, "Joy"),
                    tags = listOf("social", "colleague"),
                    relationships = listOf(Relationship(lifeId, name, "Friend", 65f, 60f)),
                    behaviorTreeState = "Idle Chat"
                ),
                NPC(
                    id = "npc_${UUID.randomUUID().toString().take(6)}",
                    name = "Marcus Vance",
                    age = 28,
                    gender = "Male",
                    ocean = OCEAN(0.8f, 0.8f, 0.4f, 0.5f, 0.4f),
                    emotion = Emotion(0.1f, 0.4f, "Calm"),
                    tags = listOf("corporate", "merchant"),
                    relationships = listOf(Relationship(lifeId, name, "Colleague", 50f, 50f)),
                    behaviorTreeState = "Working"
                )
            )
            lifeRepository.insertNpcs(initialNpcs)

            val quest = NarrativeQuestEngine.generateProceduralQuest(18, null, initialMoney)
            _uiState.value = _uiState.value.copy(activeQuests = listOf(quest))
        }
    }

    fun advanceDay() {
        val life = _uiState.value.activeLife ?: return
        viewModelScope.launch {
            // 1. Advance World Macro State
            val (nextWorld, directive) = MacroStateDirector.advanceWorldState(_uiState.value.worldState)

            // 2. Coordinate NPCs with Directive
            val updatedNpcs = CoordinationHub.decomposeAndRoute(directive, _uiState.value.npcs)
            updatedNpcs.forEach { lifeRepository.updateNpc(it) }

            // 3. Decay Needs & Health
            val decayedStats = LifestyleSocialEngine.decayNeeds(life.stats, 24f)

            // 4. Age Check (Every 365 in-game days)
            val nextAge = if (nextWorld.inGameDay % 365 == 0) life.age + 1 else life.age

            // 5. Salary if employed
            val salary = if (life.career != null) {
                val prof = CareerEconomyEngine.allProfessions.find { it.title == life.career }
                (prof?.baseSalary ?: 24_000.0) / 365.0
            } else 0.0

            val updatedLife = life.copy(
                age = nextAge,
                stats = decayedStats,
                money = life.money + salary
            )

            // Check if player died
            if (decayedStats.health <= 0f || nextAge > 100) {
                val assessment = NarrativeQuestEngine.assessLifeLegacy(updatedLife)
                _uiState.value = _uiState.value.copy(
                    activeLife = updatedLife,
                    worldState = nextWorld,
                    legacyAssessment = assessment
                )
                lifeRepository.logEvent(
                    lifeId = life.id,
                    type = "End of Life",
                    description = "Passed away at age $nextAge in ${life.country}."
                )
                return@launch
            }

            lifeRepository.updateLife(updatedLife)
            _uiState.value = _uiState.value.copy(worldState = nextWorld)
        }
    }

    fun performAction(action: String) {
        val life = _uiState.value.activeLife ?: return
        viewModelScope.launch {
            var stats = life.stats
            var money = life.money
            var career = life.career

            when (action) {
                "Eat Meal" -> {
                    stats = stats.copy(
                        hunger = (stats.hunger + 40f).coerceAtMost(100f),
                        happiness = (stats.happiness + 5f).coerceAtMost(100f)
                    )
                    money -= 15.0
                }
                "Take Shower" -> {
                    stats = stats.copy(
                        hygiene = 100f,
                        happiness = (stats.happiness + 5f).coerceAtMost(100f)
                    )
                }
                "Study Course" -> {
                    stats = stats.copy(
                        intellect = (stats.intellect + 2.5f).coerceAtMost(100f),
                        funLevel = (stats.funLevel - 10f).coerceAtLeast(0f),
                        stress = (stats.stress + 5f).coerceAtMost(100f)
                    )
                }
                "Gym Workout" -> {
                    stats = stats.copy(
                        fitness = (stats.fitness + 3f).coerceAtMost(100f),
                        health = (stats.health + 2f).coerceAtMost(100f),
                        hygiene = (stats.hygiene - 20f).coerceAtLeast(0f)
                    )
                }
                "Apply: Barista" -> career = "Barista"
                "Apply: Software Dev" -> {
                    if (stats.intellect >= 55f) {
                        career = "Junior Software Dev"
                        showToast("Hired as Junior Software Dev!")
                    } else {
                        showToast("Requires 55+ Intellect")
                    }
                }
                "Apply: AI Systems Engineer" -> {
                    val isPaid = _uiState.value.userPreferences?.isPaidUser ?: false
                    if (!isPaid) {
                        showToast("Premium Exclusive Career")
                    } else if (stats.intellect >= 80f) {
                        career = "AI Systems Engineer"
                        showToast("Hired as AI Systems Engineer!")
                    } else {
                        showToast("Requires 80+ Intellect")
                    }
                }
            }

            val updated = life.copy(stats = stats, money = money, career = career)
            lifeRepository.updateLife(updated)
            lifeRepository.logEvent(life.id, "Action", "Performed action: $action")
        }
    }

    fun playCasinoBlackjack(bet: Double) {
        val life = _uiState.value.activeLife ?: return
        if (life.money < bet) {
            showToast("Insufficient funds")
            return
        }
        viewModelScope.launch {
            val (won, payout) = CareerEconomyEngine.playBlackjack(bet)
            val newMoney = life.money - bet + payout
            val updated = life.copy(money = newMoney)
            lifeRepository.updateLife(updated)
            val desc = if (won) "Won $$payout at the Blackjack table!" else "Lost $$bet at Blackjack."
            showToast(desc)
            lifeRepository.logEvent(life.id, "Casino", desc)
        }
    }

    fun startDialogueWithNpc(npc: NPC) {
        _uiState.value = _uiState.value.copy(
            currentDialogueNpc = npc,
            dialogueHistory = listOf(Pair(npc.name, "Hello, how are you today?"))
        )
    }

    fun sendNpcDialogueMessage(message: String) {
        val npc = _uiState.value.currentDialogueNpc ?: return
        val life = _uiState.value.activeLife ?: return
        val isPaid = _uiState.value.userPreferences?.isPaidUser ?: false
        val currentModel = _uiState.value.userPreferences?.selectedAiModel ?: "claude-3-7-sonnet"

        val currentList = _uiState.value.dialogueHistory.toMutableList()
        currentList.add(Pair(life.playerName, message))
        _uiState.value = _uiState.value.copy(
            dialogueHistory = currentList,
            isAiThinking = true
        )

        viewModelScope.launch {
            val result = aiRepository.interactWithNpc(
                playerInput = message,
                npc = npc,
                playerLife = life,
                worldState = _uiState.value.worldState,
                modelName = currentModel,
                isPaidUser = isPaid
            )

            val updatedMsgs = _uiState.value.dialogueHistory.toMutableList()
            updatedMsgs.add(Pair(npc.name, result.replyText))

            val updatedNpc = npc.copy(emotion = result.updatedNpcEmotion)
            lifeRepository.updateNpc(updatedNpc)

            _uiState.value = _uiState.value.copy(
                dialogueHistory = updatedMsgs,
                currentDialogueNpc = updatedNpc,
                isAiThinking = false
            )
        }
    }

    fun exportLifeToPdf(context: Context) {
        val life = _uiState.value.activeLife ?: return
        viewModelScope.launch {
            val events = lifeRepository.getEvents(life.id)
            val file = LifeStoryPdfExporter.exportLifeToPdf(context, life, events)
            showToast("Exported PDF: ${file.name}")
        }
    }

    fun purchasePass(productId: String) {
        val life = _uiState.value.activeLife
        val userId = life?.id ?: "user_default"
        val userName = life?.playerName ?: "Player"
        viewModelScope.launch {
            val res = billingRepository.requestSubscription(productId, userId, userName)
            if (res.isSuccess) {
                showToast("Subscription request sent to Admin for review.")
            } else {
                showToast("Request failed: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun approveSubscription(requestId: String) {
        viewModelScope.launch {
            val res = billingRepository.approveSubscription(requestId, "Approved by Admin")
            if (res.isSuccess) {
                showToast("Subscription approved and unlocked!")
            }
        }
    }

    fun rejectSubscription(requestId: String) {
        viewModelScope.launch {
            val res = billingRepository.rejectSubscription(requestId, "Rejected by Admin")
            if (res.isSuccess) {
                showToast("Subscription rejected.")
            }
        }
    }

    fun createAdminCoupon(coupon: Coupon) {
        viewModelScope.launch {
            billingRepository.createAdminCoupon(coupon)
            showToast("Coupon ${coupon.code} created!")
        }
    }

    fun redeemCoupon(code: String) {
        val userId = _uiState.value.activeLife?.id ?: "user_default"
        viewModelScope.launch {
            val res = billingRepository.redeemCoupon(code, userId)
            if (res.isSuccess) {
                showToast("Coupon applied successfully!")
            } else {
                showToast("Invalid or expired coupon.")
            }
        }
    }

    fun updateSelectedAiModel(model: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateSelectedAiModel(model)
            showToast("AI Model set to: $model")
        }
    }

    fun toggleTts(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateTtsEnabled(enabled)
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateDarkMode(isDark)
        }
    }

    fun setSimulationSpeed(speed: Float) {
        viewModelScope.launch {
            userPreferencesRepository.updateSimulationSpeed(speed)
        }
    }

    private fun showToast(msg: String) {
        _uiState.value = _uiState.value.copy(toastMessage = msg)
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}

class MainViewModelFactory(
    private val lifeRepository: LifeRepository,
    private val billingRepository: BillingRepository,
    private val aiRepository: AiRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val puterBridge: PuterBridge
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                lifeRepository,
                billingRepository,
                aiRepository,
                userPreferencesRepository,
                puterBridge
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
