package com.example.domain.simulation

import com.example.data.model.Item
import com.example.data.model.Relationship
import com.example.data.model.Stats
import kotlin.random.Random

data class SocialMediaPost(
    val authorName: String,
    val content: String,
    val likes: Int,
    val isViral: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class Pet(
    val name: String,
    val species: String,          // "Golden Retriever", "Persian Cat", "African Grey Parrot"
    val happiness: Float = 80f,
    val energy: Float = 90f,
    val behaviorDescription: String = "Loyal and playful"
)

object LifestyleSocialEngine {

    /**
     * Natural Needs Decay (Hunger, Social, Hygiene, Fun)
     */
    fun decayNeeds(stats: Stats, timeScaleHours: Float = 6f): Stats {
        val hungerDecay = 3.5f * (timeScaleHours / 6f)
        val hygieneDecay = 3.0f * (timeScaleHours / 6f)
        val socialDecay = 2.5f * (timeScaleHours / 6f)
        val funDecay = 3.0f * (timeScaleHours / 6f)

        val newHunger = (stats.hunger - hungerDecay).coerceIn(0f, 100f)
        val newHygiene = (stats.hygiene - hygieneDecay).coerceIn(0f, 100f)
        val newSocial = (stats.social - socialDecay).coerceIn(0f, 100f)
        val newFun = (stats.funLevel - funDecay).coerceIn(0f, 100f)

        // Health suffers if hunger or hygiene hits 0
        var healthDelta = 0f
        if (newHunger < 15f) healthDelta -= 4f
        if (newHygiene < 10f) healthDelta -= 2f
        val newHealth = (stats.health + healthDelta).coerceIn(0f, 100f)

        // Overall happiness is weighted average of needs
        val newHappiness = ((newHunger * 0.25f) + (newHygiene * 0.2f) + (newSocial * 0.25f) + (newFun * 0.3f))
            .coerceIn(0f, 100f)

        return stats.copy(
            hunger = newHunger,
            hygiene = newHygiene,
            social = newSocial,
            funLevel = newFun,
            health = newHealth,
            happiness = newHappiness
        )
    }

    /**
     * Life Stage and Aging
     */
    fun getLifeStage(age: Int): String {
        return when {
            age < 3 -> "Infant"
            age < 12 -> "Childhood"
            age < 18 -> "Adolescence"
            age < 30 -> "Young Adult"
            age < 60 -> "Prime Adult"
            age < 80 -> "Senior"
            else -> "Elder"
        }
    }

    /**
     * Mortality Assessment
     * Calculates whether natural death occurs based on age and health
     */
    fun checkMortality(age: Int, health: Float): Boolean {
        if (health <= 0f) return true
        if (age < 50) return false

        // Mortality risk increases after 65
        val baseRisk = (age - 60) * 0.008f
        val healthPenalty = (100f - health) * 0.005f
        val totalRisk = (baseRisk + healthPenalty).coerceIn(0.01f, 0.45f)

        return Random.nextFloat() < totalRisk
    }

    /**
     * Romance Chemistry & Dating
     */
    fun calculateChemistry(playerLooks: Float, playerCharm: Float, targetAffinity: Float): Float {
        val score = (playerLooks * 0.4f) + (playerCharm * 0.35f) + (targetAffinity * 0.25f)
        return score.coerceIn(0f, 100f)
    }

    /**
     * Social Media Simulation: Post updates and calculate viral reach
     */
    fun simulateSocialMediaPost(playerFame: Float, content: String, authorName: String): SocialMediaPost {
        val baseLikes = (playerFame * 50).toInt() + Random.nextInt(5, 50)
        val viralRoll = Random.nextFloat()
        val isViral = viralRoll < (0.05f + (playerFame / 500f))
        val totalLikes = if (isViral) baseLikes * Random.nextInt(10, 100) else baseLikes

        return SocialMediaPost(
            authorName = authorName,
            content = content,
            likes = totalLikes,
            isViral = isViral
        )
    }

    /**
     * Available Lifestyle Items for purchase
     */
    val availableProperties = listOf(
        Item("PROP_1", "Cozy Studio Apartment", "Property", 120_000.0, 85f, "A comfortable starter flat in the downtown arts district."),
        Item("PROP_2", "Suburban Family Villa", "Property", 450_000.0, 92f, "Spacious two-story home with a lush garden, pool, and two-car garage."),
        Item("PROP_3", "Skyline Luxury Penthouse", "Property", 1_850_000.0, 98f, "Panoramic skyscraper penthouse with floor-to-ceiling glass and private elevator.")
    )

    val availableVehicles = listOf(
        Item("VEH_1", "Reliable Commuter Sedan", "Vehicle", 18_000.0, 80f, "Fuel-efficient and dependable for daily work transit."),
        Item("VEH_2", "Cybernetic Electric SUV", "Vehicle", 65_000.0, 95f, "Autonomous driving sensors and lightning-fast electric acceleration."),
        Item("VEH_3", "High-Performance Italian Supercar", "Vehicle", 260_000.0, 99f, "V12 roar and unmatched prestige that turns heads everywhere.")
    )

    val availablePets = listOf(
        Item("PET_1", "Golden Retriever Pup", "Pet", 1_200.0, 95f, "Affectionate, eager to please, and always ready for fetch."),
        Item("PET_2", "Persian Longhair Kitten", "Pet", 1_500.0, 90f, "Graceful, regal companion who purrs gently beside you."),
        Item("PET_3", "Exotic Talking Parrot", "Pet", 2_200.0, 92f, "Highly intelligent bird that mimics your catchphrases.")
    )
}
