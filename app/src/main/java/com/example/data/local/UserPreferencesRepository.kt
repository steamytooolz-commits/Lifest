package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_life_sim_prefs")

data class UserPreferences(
    val livesRemainingToday: Int,
    val lastResetEpochDay: Long,
    val isPaidUser: Boolean,
    val activePassType: String?,
    val passExpirationEpochMs: Long,
    val selectedAiModel: String,
    val isTtsEnabled: Boolean,
    val isDarkMode: Boolean,
    val isTurnBasedMode: Boolean,
    val simulationSpeed: Float,     // 1.0f, 2.0f, 5.0f, 0.0f (paused)
    val difficultyLevel: String,    // "Normal", "Hardcore", "Relaxed"
    val isSandboxMode: Boolean,
    val isChallengeMode: Boolean,
    val activeLifeId: String?,
    val unlockedReincarnationPerks: List<String>
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val LIVES_REMAINING = intPreferencesKey("lives_remaining_today")
        val LAST_RESET_EPOCH_DAY = longPreferencesKey("last_reset_epoch_day")
        val IS_PAID_USER = booleanPreferencesKey("is_paid_user")
        val ACTIVE_PASS_TYPE = stringPreferencesKey("active_pass_type")
        val PASS_EXPIRATION_MS = longPreferencesKey("pass_expiration_epoch_ms")
        val SELECTED_AI_MODEL = stringPreferencesKey("selected_ai_model")
        val IS_TTS_ENABLED = booleanPreferencesKey("is_tts_enabled")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val IS_TURN_BASED = booleanPreferencesKey("is_turn_based")
        val SIMULATION_SPEED = stringPreferencesKey("simulation_speed_str")
        val DIFFICULTY_LEVEL = stringPreferencesKey("difficulty_level")
        val IS_SANDBOX = booleanPreferencesKey("is_sandbox")
        val IS_CHALLENGE = booleanPreferencesKey("is_challenge")
        val ACTIVE_LIFE_ID = stringPreferencesKey("active_life_id")
        val UNLOCKED_PERKS = stringPreferencesKey("unlocked_perks_csv")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.userDataStore.data.map { prefs ->
        val currentEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val lastResetDay = prefs[PreferencesKeys.LAST_RESET_EPOCH_DAY] ?: currentEpochDay
        var lives = prefs[PreferencesKeys.LIVES_REMAINING] ?: 3

        // If day changed, reset lives for free users
        if (currentEpochDay > lastResetDay) {
            lives = 3
        }

        val passExpiration = prefs[PreferencesKeys.PASS_EXPIRATION_MS] ?: 0L
        val isPaid = (prefs[PreferencesKeys.IS_PAID_USER] ?: false) || (passExpiration > System.currentTimeMillis())

        val perksCsv = prefs[PreferencesKeys.UNLOCKED_PERKS] ?: ""
        val perksList = if (perksCsv.isBlank()) emptyList() else perksCsv.split(",")

        UserPreferences(
            livesRemainingToday = if (isPaid) 999 else lives,
            lastResetEpochDay = lastResetDay,
            isPaidUser = isPaid,
            activePassType = prefs[PreferencesKeys.ACTIVE_PASS_TYPE],
            passExpirationEpochMs = passExpiration,
            selectedAiModel = prefs[PreferencesKeys.SELECTED_AI_MODEL] ?: "claude-3-7-sonnet",
            isTtsEnabled = prefs[PreferencesKeys.IS_TTS_ENABLED] ?: false,
            isDarkMode = prefs[PreferencesKeys.IS_DARK_MODE] ?: true,
            isTurnBasedMode = prefs[PreferencesKeys.IS_TURN_BASED] ?: false,
            simulationSpeed = prefs[PreferencesKeys.SIMULATION_SPEED]?.toFloatOrNull() ?: 1.0f,
            difficultyLevel = prefs[PreferencesKeys.DIFFICULTY_LEVEL] ?: "Adaptive",
            isSandboxMode = prefs[PreferencesKeys.IS_SANDBOX] ?: false,
            isChallengeMode = prefs[PreferencesKeys.IS_CHALLENGE] ?: false,
            activeLifeId = prefs[PreferencesKeys.ACTIVE_LIFE_ID],
            unlockedReincarnationPerks = perksList
        )
    }

    suspend fun consumeLife(): Boolean {
        val prefs = userPreferencesFlow.first()
        if (prefs.isPaidUser) return true
        if (prefs.livesRemainingToday <= 0) return false

        context.userDataStore.edit { preferences ->
            val currentLives = preferences[PreferencesKeys.LIVES_REMAINING] ?: 3
            preferences[PreferencesKeys.LIVES_REMAINING] = (currentLives - 1).coerceAtLeast(0)
        }
        return true
    }

    suspend fun resetDailyLives() {
        val currentEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.LIVES_REMAINING] = 3
            preferences[PreferencesKeys.LAST_RESET_EPOCH_DAY] = currentEpochDay
        }
    }

    suspend fun setPassEntitlement(productId: String, durationDays: Int) {
        val now = System.currentTimeMillis()
        val expiration = if (durationDays == -1) {
            // Lifetime
            Long.MAX_VALUE
        } else {
            now + (durationDays.toLong() * 24 * 60 * 60 * 1000)
        }

        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PAID_USER] = true
            preferences[PreferencesKeys.ACTIVE_PASS_TYPE] = productId
            preferences[PreferencesKeys.PASS_EXPIRATION_MS] = expiration
        }
    }

    suspend fun checkAndExpirePasses() {
        val now = System.currentTimeMillis()
        context.userDataStore.edit { preferences ->
            val expiration = preferences[PreferencesKeys.PASS_EXPIRATION_MS] ?: 0L
            if (expiration in 1..now) {
                preferences[PreferencesKeys.IS_PAID_USER] = false
                preferences.remove(PreferencesKeys.ACTIVE_PASS_TYPE)
                preferences[PreferencesKeys.PASS_EXPIRATION_MS] = 0L
            }
        }
    }

    suspend fun updateSelectedAiModel(model: String) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_AI_MODEL] = model
        }
    }

    suspend fun updateTtsEnabled(enabled: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_TTS_ENABLED] = enabled
        }
    }

    suspend fun updateDarkMode(isDark: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDark
        }
    }

    suspend fun updateSimulationSpeed(speed: Float) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.SIMULATION_SPEED] = speed.toString()
        }
    }

    suspend fun updateTurnBasedMode(isTurnBased: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_TURN_BASED] = isTurnBased
        }
    }

    suspend fun setActiveLifeId(lifeId: String?) {
        context.userDataStore.edit { preferences ->
            if (lifeId == null) {
                preferences.remove(PreferencesKeys.ACTIVE_LIFE_ID)
            } else {
                preferences[PreferencesKeys.ACTIVE_LIFE_ID] = lifeId
            }
        }
    }

    suspend fun addReincarnationPerk(perk: String) {
        context.userDataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.UNLOCKED_PERKS] ?: ""
            val list = if (current.isBlank()) mutableListOf() else current.split(",").toMutableList()
            if (!list.contains(perk)) {
                list.add(perk)
                preferences[PreferencesKeys.UNLOCKED_PERKS] = list.joinToString(",")
            }
        }
    }
}
