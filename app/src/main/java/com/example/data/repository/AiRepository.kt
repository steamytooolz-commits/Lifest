package com.example.data.repository

import com.example.data.model.Emotion
import com.example.data.model.Life
import com.example.data.model.Memory
import com.example.data.model.NPC
import com.example.data.model.Relationship
import com.example.data.model.WorldState
import com.example.data.remote.FallbackApiClient
import com.example.data.remote.OpenAiChatRequest
import com.example.data.remote.OpenAiMessage
import com.example.data.remote.PuterBridge
import com.example.domain.ai.EmotionEngine
import com.example.domain.ai.MemoryEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class DialogueResult(
    val replyText: String,
    val updatedNpcEmotion: Emotion,
    val affinityDelta: Float,
    val trustDelta: Float,
    val detectedPromise: String? = null,
    val isFromFallback: Boolean = false
)

class AiRepository(
    private val puterBridge: PuterBridge,
    private val lifeRepository: LifeRepository
) {

    suspend fun interactWithNpc(
        playerInput: String,
        npc: NPC,
        playerLife: Life,
        worldState: WorldState,
        modelName: String = "claude-sonnet-4-6",
        isPaidUser: Boolean = false
    ): DialogueResult = withContext(Dispatchers.IO) {
        // 1. Retrieve top-K relevant memories
        val allMemories = lifeRepository.getMemoriesForNpc(npc.id, playerLife.id)
        val relevantMemories = MemoryEngine.retrieveTopKMemories(playerInput, allMemories, topK = if (isPaidUser) 10 else 5)
        val memoriesText = MemoryEngine.formatMemoriesForPrompt(relevantMemories)

        // Find relationship with player
        val playerRel = npc.relationships.find { it.targetId == playerLife.id }
            ?: Relationship(targetId = playerLife.id, targetName = playerLife.playerName, type = "Acquaintance")

        // 2. Build structured system & context prompt
        val systemPrompt = """
            You are ${npc.name}, a ${npc.age}-year-old ${npc.gender} living in ${playerLife.country}.
            Personality (OCEAN traits 0-1):
            - Openness: ${npc.ocean.openness}
            - Conscientiousness: ${npc.ocean.conscientiousness}
            - Extraversion: ${npc.ocean.extraversion}
            - Agreeableness: ${npc.ocean.agreeableness}
            - Neuroticism: ${npc.ocean.neuroticism}
            Current Emotion: ${npc.emotion.label} (Valence: ${npc.emotion.valence}, Arousal: ${npc.emotion.arousal})
            Relationship with player (${playerLife.playerName}): ${playerRel.type}, Affinity: ${playerRel.affinity}/100, Trust: ${playerRel.trust}/100, Rivalry: ${playerRel.rivalry}/100
            World Context: Season: ${worldState.inGameSeason}, Weather: ${worldState.weather}, Economy: ${worldState.economyTrend}.
            Past Memories with player:
            $memoriesText

            Instructions:
            1. Respond naturally in first-person dialogue as ${npc.name} (1-3 sentences).
            2. Match your tone to your OCEAN personality traits and current emotion.
            3. Reflect past memories if relevant.
            4. At the very end of your response on a new line, output JSON format:
            {"valenceDelta": <-0.3 to 0.3>, "affinityDelta": <-10 to 10>, "trustDelta": <-10 to 10>, "promiseMade": "<detected promise or null>"}
        """.trimIndent()

        val fullPrompt = "$systemPrompt\n\nPlayer (${playerLife.playerName}) says: \"$playerInput\"\n\n${npc.name}:"

        var rawResponse = ""
        var isFallback = false

        // Try Puter.js bridge first
        try {
            rawResponse = puterBridge.chat(fullPrompt, modelName)
        } catch (e: Exception) {
            // Fallback: Use built-in simulated intelligent agent or OpenAI API
            try {
                val openAiResponse = FallbackApiClient.api.createChatCompletion(
                    authHeader = "Bearer dummy_key",
                    request = OpenAiChatRequest(
                        model = "gpt-4o-mini",
                        messages = listOf(
                            OpenAiMessage(role = "system", content = systemPrompt),
                            OpenAiMessage(role = "user", content = playerInput)
                        )
                    )
                )
                rawResponse = openAiResponse.choices?.firstOrNull()?.message?.content ?: ""
            } catch (fallbackError: Exception) {
                isFallback = true
                rawResponse = generateProceduralNpcReply(playerInput, npc, playerRel)
            }
        }

        // Parse response and extract delta
        val parsed = parseDialogueResponse(rawResponse, npc)

        // Save conversation as memory
        val memory = Memory(
            id = UUID.randomUUID().toString(),
            npcId = npc.id,
            lifeId = playerLife.id,
            content = "Player said: \"$playerInput\". I replied: \"${parsed.replyText}\"",
            timestamp = System.currentTimeMillis(),
            valence = parsed.updatedNpcEmotion.valence,
            arousal = parsed.updatedNpcEmotion.arousal,
            importance = if (parsed.detectedPromise != null) 0.9f else 0.5f,
            tags = listOfNotNull("dialogue", if (parsed.detectedPromise != null) "promise" else null)
        )
        lifeRepository.addMemory(memory, isPaidUser)

        parsed.copy(isFromFallback = isFallback)
    }

    private fun generateProceduralNpcReply(input: String, npc: NPC, rel: Relationship): String {
        val tone = when {
            npc.ocean.agreeableness > 0.6f -> "warmly"
            npc.ocean.neuroticism > 0.7f -> "anxiously"
            npc.emotion.valence < -0.3f -> "curtly"
            else -> "thoughtfully"
        }

        val greetings = listOf(
            "It's good to see you, actually. What's on your mind?",
            "Hey, I was just thinking about recent events. Did you hear the news?",
            "I have to admit, things have been quite unpredictable around here lately.",
            "Always a pleasure. Let's make sure we stay focused on our goals."
        )
        val reply = greetings.random()
        return "$reply\n{\"valenceDelta\": 0.1, \"affinityDelta\": 2, \"trustDelta\": 1, \"promiseMade\": null}"
    }

    private fun parseDialogueResponse(raw: String, npc: NPC): DialogueResult {
        var replyText = raw.trim()
        var valenceDelta = 0.05f
        var affinityDelta = 1f
        var trustDelta = 1f
        var promise: String? = null

        try {
            val jsonIndex = raw.lastIndexOf("{")
            if (jsonIndex != -1) {
                val jsonPart = raw.substring(jsonIndex)
                replyText = raw.substring(0, jsonIndex).trim()

                if (jsonPart.contains("valenceDelta")) {
                    val vMatch = Regex("\"valenceDelta\"\\s*:\\s*([-\\d.]+)").find(jsonPart)
                    vMatch?.groupValues?.get(1)?.toFloatOrNull()?.let { valenceDelta = it }
                }
                if (jsonPart.contains("affinityDelta")) {
                    val aMatch = Regex("\"affinityDelta\"\\s*:\\s*([-\\d.]+)").find(jsonPart)
                    aMatch?.groupValues?.get(1)?.toFloatOrNull()?.let { affinityDelta = it }
                }
                if (jsonPart.contains("trustDelta")) {
                    val tMatch = Regex("\"trustDelta\"\\s*:\\s*([-\\d.]+)").find(jsonPart)
                    tMatch?.groupValues?.get(1)?.toFloatOrNull()?.let { trustDelta = it }
                }
                if (jsonPart.contains("promiseMade")) {
                    val pMatch = Regex("\"promiseMade\"\\s*:\\s*\"([^\"]+)\"").find(jsonPart)
                    pMatch?.groupValues?.get(1)?.let {
                        if (it != "null" && it.isNotBlank()) promise = it
                    }
                }
            }
        } catch (e: Exception) {
            // Keep defaults
        }

        val updatedEmotion = EmotionEngine.calculateEmotionalResponse(
            currentEmotion = npc.emotion,
            rawValenceDelta = valenceDelta,
            rawArousalDelta = 0.05f,
            ocean = npc.ocean
        )

        return DialogueResult(
            replyText = replyText.ifBlank { "I hear you. Let's see how things play out." },
            updatedNpcEmotion = updatedEmotion,
            affinityDelta = affinityDelta,
            trustDelta = trustDelta,
            detectedPromise = promise
        )
    }

    suspend fun generateCharacterSceneImage(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = puterBridge.txt2img(prompt)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateSpeechAudio(text: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = puterBridge.txt2speech(text)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
