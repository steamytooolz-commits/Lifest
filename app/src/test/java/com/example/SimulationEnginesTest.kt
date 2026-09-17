package com.example

import com.example.data.model.Coupon
import com.example.data.model.Emotion
import com.example.data.model.Life
import com.example.data.model.Memory
import com.example.data.model.NPC
import com.example.data.model.OCEAN
import com.example.data.model.Stats
import com.example.data.model.WorldState
import com.example.data.remote.FirestoreCouponService
import com.example.domain.ai.CoordinationHub
import com.example.domain.ai.EmotionEngine
import com.example.domain.ai.MacroStateDirector
import com.example.domain.ai.MemoryEngine
import com.example.domain.simulation.CareerEconomyEngine
import com.example.domain.simulation.LifestyleSocialEngine
import com.example.domain.simulation.NarrativeQuestEngine
import com.example.domain.simulation.WorldGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulationEnginesTest {

    @Test
    fun `test EmotionEngine circumplex mapping and OCEAN modulation`() {
        val joyLabel = EmotionEngine.getCircumplexLabel(valence = 0.8f, arousal = 0.8f)
        assertEquals("Euphoric", joyLabel)

        val sereneLabel = EmotionEngine.getCircumplexLabel(valence = 0.7f, arousal = 0.3f)
        assertEquals("Serene", sereneLabel)

        val sadLabel = EmotionEngine.getCircumplexLabel(valence = -0.5f, arousal = 0.3f)
        assertEquals("Sad", sadLabel)

        val baselineEmotion = Emotion(0.0f, 0.5f, "Neutral", 0.5f)
        val highNeuroticism = OCEAN(0.5f, 0.5f, 0.5f, 0.5f, 0.95f)

        // Negative event should be amplified by high neuroticism
        val response = EmotionEngine.calculateEmotionalResponse(
            currentEmotion = baselineEmotion,
            rawValenceDelta = -0.2f,
            rawArousalDelta = 0.1f,
            ocean = highNeuroticism
        )
        assertTrue(response.valence < -0.2f)
    }

    @Test
    fun `test MemoryEngine top-K relevance and recency retrieval`() {
        val now = System.currentTimeMillis()
        val memories = listOf(
            Memory("1", "npc1", "life1", "We walked in the botanical garden and discussed philosophy.", now - 10000, 0.5f, 0.3f, 0.6f, listOf("walk", "garden")),
            Memory("2", "npc1", "life1", "You loaned me 500 dollars to fix my car.", now - 5000, -0.2f, 0.6f, 0.9f, listOf("loan", "car", "debt")),
            Memory("3", "npc1", "life1", "We attended the annual university graduation together.", now - 200000, 0.7f, 0.5f, 0.7f, listOf("graduation", "school"))
        )

        val query = "Can you repay the car loan you borrowed?"
        val topMemories = MemoryEngine.retrieveTopKMemories(query, memories, topK = 2)

        assertEquals(2, topMemories.size)
        // Memory 2 about car loan should be the most relevant
        assertEquals("2", topMemories[0].id)
    }

    @Test
    fun `test MacroStateDirector advances day and emits valid directive`() {
        val initialState = WorldState(inGameDay = 1, inGameYear = 2026, inGameSeason = "Spring")
        val (nextState, directive) = MacroStateDirector.advanceWorldState(initialState)

        assertEquals(2, nextState.inGameDay)
        assertEquals(2, directive.day)
        assertNotNull(directive.economicShift)
        assertTrue(directive.rawJson.contains("\"inGameDay\": 2"))
    }

    @Test
    fun `test CoordinationHub routes directives to tag-driven NPCs`() {
        val (_, directive) = MacroStateDirector.advanceWorldState(WorldState(inGameDay = 10))
        val npcs = listOf(
            NPC(
                id = "npc_trader",
                name = "Kip",
                age = 35,
                gender = "Male",
                ocean = OCEAN(0.5f, 0.8f, 0.6f, 0.5f, 0.3f),
                emotion = Emotion(0f, 0.5f, "Neutral", 0.5f),
                tags = listOf("merchant"),
                relationships = emptyList(),
                behaviorTreeState = "Idle"
            )
        )

        val updatedNpcs = CoordinationHub.decomposeAndRoute(directive, npcs)
        assertEquals(1, updatedNpcs.size)
        assertFalse(updatedNpcs[0].behaviorTreeState == "Idle")
    }

    @Test
    fun `test FirestoreCouponService validation and redemption`() = runBlocking {
        val service = FirestoreCouponService()

        // Valid community coupon
        val result = service.validateAndRedeemCoupon("FREEPASS2026", "test_user_1")
        assertTrue(result.isSuccess)
        val redemption = result.getOrThrow()
        assertEquals("FREEPASS2026", redemption.coupon.code)
        assertEquals(1, redemption.coupon.usedCount)

        // Invalid coupon
        val invalidResult = service.validateAndRedeemCoupon("NON_EXISTENT_CODE", "test_user_1")
        assertTrue(invalidResult.isFailure)
    }

    @Test
    fun `test CareerEconomyEngine professions and casino blackjack`() {
        val freeCareers = CareerEconomyEngine.getAvailableProfessions(isPaidUser = false)
        assertEquals(10, freeCareers.size)

        val paidCareers = CareerEconomyEngine.getAvailableProfessions(isPaidUser = true)
        assertTrue(paidCareers.size >= 40)

        val (won, returnAmt) = CareerEconomyEngine.playBlackjack(100.0)
        if (won) {
            assertEquals(200.0, returnAmt, 0.01)
        } else {
            assertEquals(0.0, returnAmt, 0.01)
        }
    }

    @Test
    fun `test NarrativeQuestEngine procedural quest and legacy assessment`() {
        val quest = NarrativeQuestEngine.generateProceduralQuest(25, "Software Architect", 45_000.0)
        assertNotNull(quest.title)
        assertTrue(quest.rewardMoney >= 0.0)

        val sampleLife = Life(
            id = "test_life",
            playerName = "Marcus Aurelius",
            age = 82,
            country = "Italy",
            traits = listOf("Genius", "Resilient"),
            stats = Stats(health = 0f, karma = 95f),
            money = 500_000.0,
            career = "Emperor & Philosopher",
            relationships = emptyList(),
            inventory = emptyList(),
            isActive = false,
            createdAt = 0L
        )

        val legacy = NarrativeQuestEngine.assessLifeLegacy(sampleLife)
        assertTrue(legacy.score >= 500)
        assertNotNull(legacy.unlockedPerk)
    }

    @Test
    fun `test WorldGenerator 25 free nations vs 195 paid nations`() {
        val freeNations = WorldGenerator.getCountriesForUser(isPaidUser = false)
        assertEquals(25, freeNations.size)

        val allNations = WorldGenerator.getCountriesForUser(isPaidUser = true)
        assertTrue(allNations.size > 25)
    }

    @Test
    fun `test Ed25519 offline coupon verification failure on tampered signature`() {
        // Tampered signature or invalid public key should fail cleanly
        val isValid = com.example.crypto.Ed25519Util.verifySignature(
            publicKeyBase64 = "MCowBQYDK2VwAyEA1111111111111111111111111111111111111111110=",
            payloadJson = "{\"code\":\"TEST100\"}",
            signatureBase64 = "invalid_signature"
        )
        assertFalse(isValid)
    }
}
