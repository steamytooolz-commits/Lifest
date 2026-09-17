package com.example.domain.simulation

import com.example.data.model.Life
import com.example.data.model.Quest
import kotlin.random.Random

data class LegacyAssessment(
    val score: Int,                 // 0..1000
    val title: String,              // e.g., "Legendary Visionary", "Distinguished Citizen", "Modest Wanderer"
    val summary: String,
    val unlockedPerk: String
)

object NarrativeQuestEngine {

    /**
     * Procedural Quest Factory emitting valid JSON schema and Quest model
     */
    fun generateProceduralQuest(playerAge: Int, career: String?, money: Double): Quest {
        val questTypes = listOf("Career", "Social", "Wealth", "Civic", "Exploration")
        val chosenType = questTypes.random()

        return when (chosenType) {
            "Career" -> {
                val target = if (career == null) "Land a stable profession" else "Secure a promotion or executive raise"
                Quest(
                    id = "Q_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
                    title = "Professional Horizon",
                    description = "Take proactive initiative at your workplace or send out competitive applications.",
                    targetType = "Career",
                    targetGoal = target,
                    rewardMoney = 1_500.0,
                    rewardKarma = 5f
                )
            }
            "Social" -> {
                Quest(
                    id = "Q_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
                    title = "Deepening Ties",
                    description = "Reconnect with an acquaintance or resolve an existing grudge over coffee.",
                    targetType = "Social",
                    targetGoal = "Raise an NPC relationship trust above 75",
                    rewardMoney = 500.0,
                    rewardKarma = 10f
                )
            }
            "Civic" -> {
                Quest(
                    id = "Q_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
                    title = "Community Philanthropy",
                    description = "Support local environmental restoration or city heritage projects.",
                    targetType = "Civic",
                    targetGoal = "Donate to community shelter or plant neighborhood trees",
                    rewardMoney = 0.0,
                    rewardKarma = 20f
                )
            }
            "Wealth" -> {
                Quest(
                    id = "Q_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
                    title = "Financial Fortification",
                    description = "Build an emergency fund or invest in dividend-bearing equity.",
                    targetType = "Wealth",
                    targetGoal = "Amass at least $50,000 in liquid assets",
                    rewardMoney = 3_000.0,
                    rewardKarma = 5f
                )
            }
            else -> {
                Quest(
                    id = "Q_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
                    title = "Cultural Discovery",
                    description = "Embark on an international journey to experience foreign traditions.",
                    targetType = "Exploration",
                    targetGoal = "Travel to a new continent or learn a new dialect",
                    rewardMoney = 1_000.0,
                    rewardKarma = 15f
                )
            }
        }
    }

    /**
     * Crime & Law Enforcement Simulation
     */
    fun attemptCrime(crimeType: String, playerIntellect: Float): Triple<Boolean, Double, Int> {
        val (baseSuccess, loot, prisonYears) = when (crimeType) {
            "Petty Theft" -> Triple(0.70f, Random.nextDouble(50.0, 300.0), 1)
            "Burglary" -> Triple(0.45f, Random.nextDouble(2_000.0, 10_000.0), 3)
            "Financial Embezzlement" -> Triple(0.35f, Random.nextDouble(25_000.0, 100_000.0), 5)
            else -> Triple(0.50f, 1_000.0, 2)
        }

        val successRate = (baseSuccess + (playerIntellect / 300f)).coerceIn(0.15f, 0.90f)
        val succeeded = Random.nextFloat() < successRate

        return if (succeeded) {
            Triple(true, loot, 0)
        } else {
            Triple(false, 0.0, prisonYears)
        }
    }

    /**
     * Legacy Assessment upon player death
     */
    fun assessLifeLegacy(life: Life): LegacyAssessment {
        var score = 0

        // Age factor
        score += (life.age * 5).coerceAtMost(350)

        // Wealth factor
        score += when {
            life.money > 1_000_000 -> 300
            life.money > 250_000 -> 200
            life.money > 50_000 -> 100
            else -> 30
        }

        // Karma & Civic factor
        score += (life.stats.karma * 2).toInt()

        // Relationships
        val healthyRelationships = life.relationships.count { it.affinity > 70 }
        score += healthyRelationships * 25

        val (title, perk) = when {
            score >= 750 -> Pair("Legendary luminary", "Inherited Fortitude (+15% health & wealth in next life)")
            score >= 500 -> Pair("Distinguished Citizen", "Charismatic Charm (+10% initial affinity in next life)")
            score >= 300 -> Pair("Respected Pathfinder", "Intellectual Acumen (+10% initial intellect in next life)")
            else -> Pair("Modest Wanderer", "Tenacious Will (+5% all baseline attributes in next life)")
        }

        val summary = "${life.playerName} lived to the age of ${life.age} in ${life.country}. " +
                "They pursued a career as ${life.career ?: "an independent soul"} and accumulated $${String.format("%,.0f", life.money)}. " +
                "Their deeds left a lasting legacy of $title."

        return LegacyAssessment(
            score = score.coerceIn(0, 1000),
            title = title,
            summary = summary,
            unlockedPerk = perk
        )
    }
}
