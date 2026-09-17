package com.example.domain.ai

import com.example.data.model.NPC
import com.example.data.model.Relationship
import kotlin.random.Random

data class NpcDomainInstruction(
    val domain: String,             // "Career", "Health", "Social", "Governance", "Resources"
    val behaviorGoal: String,
    val moodImpactValence: Float,
    val moodImpactArousal: Float
)

object CoordinationHub {

    /**
     * Decomposes macro directives into domain instructions and assigns to NPC groups
     */
    fun decomposeAndRoute(directive: MacroDirective, npcs: List<NPC>): List<NPC> {
        val domainMap = mutableMapOf<String, NpcDomainInstruction>()

        // 1. Economic / Career Domain
        when (directive.economicShift) {
            "Boom" -> domainMap["merchant"] = NpcDomainInstruction("Career", "Expand inventory and seek investment", 0.3f, 0.2f)
            "Market Crash" -> domainMap["merchant"] = NpcDomainInstruction("Career", "Slash prices, liquidate debt and panic sell", -0.4f, 0.4f)
            "Recession" -> domainMap["corporate"] = NpcDomainInstruction("Career", "Hiring freeze and cost cutting", -0.2f, 0.1f)
            else -> domainMap["merchant"] = NpcDomainInstruction("Career", "Standard trading operations", 0.0f, 0.0f)
        }

        // 2. Weather & Resource Domain
        if (directive.weatherCondition == "Stormy" || directive.weatherCondition == "Blizzard") {
            domainMap["farmer"] = NpcDomainInstruction("Resources", "Protect crops and shelter livestock", -0.2f, 0.3f)
        } else if (directive.resourceMultiplier < 0.8f) {
            domainMap["farmer"] = NpcDomainInstruction("Resources", "Ration food supplies and prepare for shortages", -0.3f, 0.2f)
        }

        // 3. Health & Disaster Domain
        if (directive.disasterAlert != null) {
            domainMap["doctor"] = NpcDomainInstruction("Health", "Mobilize triage for ${directive.disasterAlert}", -0.1f, 0.5f)
            domainMap["young_adult"] = NpcDomainInstruction("Social", "Evacuate and seek safe shelters", -0.3f, 0.5f)
        }

        // Apply domain instructions and execute behavior routines
        return npcs.map { npc ->
            var updatedNpc = npc

            // Check matching tags
            for (tag in npc.tags) {
                val instruction = domainMap[tag.lowercase()]
                if (instruction != null) {
                    val newEmotion = EmotionEngine.calculateEmotionalResponse(
                        currentEmotion = updatedNpc.emotion,
                        rawValenceDelta = instruction.moodImpactValence,
                        rawArousalDelta = instruction.moodImpactArousal,
                        ocean = updatedNpc.ocean
                    )
                    updatedNpc = updatedNpc.copy(
                        emotion = newEmotion,
                        behaviorTreeState = instruction.behaviorGoal
                    )
                    break
                }
            }

            // Simulate routine behavior state
            if (updatedNpc.behaviorTreeState.isBlank() || updatedNpc.behaviorTreeState == "Idle") {
                val routine = when (Random.nextInt(4)) {
                    0 -> "Working Routine"
                    1 -> "Socializing at Cafe"
                    2 -> "Resting at Home"
                    else -> "Pursuing Personal Hobby"
                }
                updatedNpc = updatedNpc.copy(behaviorTreeState = routine)
            }

            updatedNpc
        }
    }

    /**
     * Autonomous NPC-to-NPC interactions without player involvement:
     * Simulates conversation, gossip exchange, and relationship drift.
     */
    fun simulateNpcToNpcSocialNetwork(npcs: List<NPC>): Pair<List<NPC>, List<String>> {
        if (npcs.size < 2) return Pair(npcs, emptyList())
        val updatedList = npcs.toMutableList()
        val gossipEvents = mutableListOf<String>()

        // Pick 2 random NPCs to interact
        val idxA = Random.nextInt(npcs.size)
        var idxB = Random.nextInt(npcs.size)
        if (idxA == idxB) {
            idxB = (idxA + 1) % npcs.size
        }

        val npcA = updatedList[idxA]
        val npcB = updatedList[idxB]

        // Check compatibility based on Agreeableness & Extraversion
        val harmony = (npcA.ocean.agreeableness + npcB.ocean.agreeableness) / 2.0f
        val isConflict = (npcA.ocean.neuroticism > 0.7f && npcB.ocean.neuroticism > 0.7f) || harmony < 0.3f

        if (isConflict) {
            // Dispute/rivalry
            val newAEmotion = EmotionEngine.calculateEmotionalResponse(npcA.emotion, -0.2f, 0.3f, npcA.ocean)
            val newBEmotion = EmotionEngine.calculateEmotionalResponse(npcB.emotion, -0.2f, 0.3f, npcB.ocean)
            updatedList[idxA] = npcA.copy(emotion = newAEmotion, behaviorTreeState = "Resolving Dispute with ${npcB.name}")
            updatedList[idxB] = npcB.copy(emotion = newBEmotion, behaviorTreeState = "Resolving Dispute with ${npcA.name}")
            gossipEvents.add("${npcA.name} and ${npcB.name} had a heated disagreement regarding politics.")
        } else {
            // Friendly chat and emotion contagion
            val contagiousB = EmotionEngine.simulateContagion(npcB.emotion, npcA.emotion, npcB.ocean)
            val contagiousA = EmotionEngine.simulateContagion(npcA.emotion, npcB.emotion, npcA.ocean)
            updatedList[idxA] = npcA.copy(emotion = contagiousA, behaviorTreeState = "Chatted warmly with ${npcB.name}")
            updatedList[idxB] = npcB.copy(emotion = contagiousB, behaviorTreeState = "Shared gossip with ${npcA.name}")
            gossipEvents.add("${npcA.name} and ${npcB.name} caught up over coffee and discussed neighborhood news.")
        }

        return Pair(updatedList, gossipEvents)
    }
}
