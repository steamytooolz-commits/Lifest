package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Stats(
    val hunger: Float = 100f,      // 0..100
    val social: Float = 100f,      // 0..100
    val hygiene: Float = 100f,     // 0..100
    val funLevel: Float = 100f,    // 0..100 (fun)
    val health: Float = 100f,      // 0..100
    val fitness: Float = 50f,      // 0..100
    val intellect: Float = 50f,    // 0..100
    val looks: Float = 50f,        // 0..100
    val happiness: Float = 80f,    // 0..100
    val karma: Float = 50f,        // 0..100
    val stress: Float = 10f,       // 0..100
    val civicIndex: Float = 50f,   // 0..100 (Civic standing)
    val fame: Float = 0f,          // 0..100
    val addictionLevel: Float = 0f // 0..100
)

@JsonClass(generateAdapter = true)
data class Relationship(
    val targetId: String,
    val targetName: String,
    val type: String,               // "Parent", "Sibling", "Friend", "Partner", "Rival", "Colleague", "Mentor"
    val affinity: Float = 50f,      // 0..100
    val trust: Float = 50f,         // 0..100
    val romance: Float = 0f,        // 0..100
    val rivalry: Float = 0f,        // 0..100
    val debt: Double = 0.0          // Money owed between them
)

@JsonClass(generateAdapter = true)
data class Item(
    val id: String,
    val name: String,
    val type: String,               // "Vehicle", "Property", "Pet", "Luxury", "Tool", "Book"
    val value: Double,
    val quality: Float = 100f,
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class OCEAN(
    val openness: Float = 0.5f,
    val conscientiousness: Float = 0.5f,
    val extraversion: Float = 0.5f,
    val agreeableness: Float = 0.5f,
    val neuroticism: Float = 0.5f
)

@JsonClass(generateAdapter = true)
data class Emotion(
    val valence: Float = 0.0f,     // -1.0 (Very negative) to +1.0 (Very positive)
    val arousal: Float = 0.5f,     // 0.0 (Calm/Lethargic) to 1.0 (Intense/Excited)
    val label: String = "Neutral", // Russell's quadrant label e.g., Joy, Serenity, Anger, Sadness
    val intensity: Float = 0.5f
)

@Entity(tableName = "lives")
@JsonClass(generateAdapter = true)
data class Life(
    @PrimaryKey val id: String,
    val playerName: String,
    val age: Int,
    val country: String,
    val traits: List<String>,
    val stats: Stats,
    val money: Double,
    val career: String?,
    val relationships: List<Relationship>,
    val inventory: List<Item>,
    val isActive: Boolean,
    val createdAt: Long
)

@Entity(tableName = "npcs")
@JsonClass(generateAdapter = true)
data class NPC(
    @PrimaryKey val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val ocean: OCEAN,
    val emotion: Emotion,
    val tags: List<String>,
    val relationships: List<Relationship>,
    val behaviorTreeState: String
)

@Entity(tableName = "memories")
@JsonClass(generateAdapter = true)
data class Memory(
    @PrimaryKey val id: String,
    val npcId: String,
    val lifeId: String,
    val content: String,
    val timestamp: Long,
    val valence: Float,
    val arousal: Float,
    val importance: Float,
    val tags: List<String>
)

@Entity(tableName = "events")
@JsonClass(generateAdapter = true)
data class Event(
    @PrimaryKey val id: String,
    val lifeId: String,
    val type: String,
    val description: String,
    val timestamp: Long,
    val consequences: String
)

@JsonClass(generateAdapter = true)
data class Coupon(
    val code: String,
    val discountPercent: Int?,
    val fixedAmount: Double?,
    val expirationDate: Long,
    val maxUses: Int,
    val usedCount: Int,
    val applicableProductIds: List<String>
)

@JsonClass(generateAdapter = true)
data class Entitlement(
    val userId: String,
    val productId: String,
    val purchaseToken: String,
    val startDate: Long,
    val endDate: Long?,
    val isActive: Boolean
)

enum class SubscriptionStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@JsonClass(generateAdapter = true)
data class SubscriptionRequest(
    val id: String,
    val userId: String,
    val userName: String,
    val productId: String,
    val productTitle: String,
    val priceString: String,
    val durationDays: Int,
    val requestTimestamp: Long = System.currentTimeMillis(),
    val status: SubscriptionStatus = SubscriptionStatus.PENDING,
    val adminNotes: String? = null
)

@JsonClass(generateAdapter = true)
data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val targetType: String,        // "Career", "Social", "Skill", "Wealth", "Civic"
    val targetGoal: String,
    val rewardMoney: Double,
    val rewardKarma: Float,
    val isCompleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class WorldState(
    val inGameDay: Int = 1,
    val inGameSeason: String = "Spring",
    val inGameYear: Int = 2026,
    val economyTrend: String = "Stable", // "Boom", "Stable", "Recession", "Depression"
    val inflationRate: Float = 0.025f,
    val politicalClimate: String = "Democracy", // "Peaceful", "Election Year", "Civil Tension"
    val weather: String = "Sunny",       // "Sunny", "Rainy", "Stormy", "Heatwave", "Blizzard"
    val resourceLevel: Float = 1.0f,     // 0.2 (Scarcity) to 1.5 (Abundant)
    val activeDisaster: String? = null    // null or "Flood", "Wildfire", "Pandemic"
)
