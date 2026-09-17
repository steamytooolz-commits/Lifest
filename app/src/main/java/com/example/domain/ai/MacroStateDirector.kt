package com.example.domain.ai

import com.example.data.model.WorldState
import kotlin.random.Random

data class MacroDirective(
    val day: Int,
    val economicShift: String,
    val inflationRate: Float,
    val weatherCondition: String,
    val politicalDirective: String,
    val resourceMultiplier: Float,
    val disasterAlert: String?,
    val rawJson: String
)

object MacroStateDirector {

    private val seasons = listOf("Spring", "Summer", "Autumn", "Winter")
    private val weathers = listOf("Sunny", "Rainy", "Cloudy", "Stormy", "Heatwave", "Blizzard")
    private val economyTrends = listOf("Boom", "Stable", "Recession", "Market Crash", "Recovery")
    private val politicalClimates = listOf("Stable Democracy", "Election Season", "Civil Protests", "Technological Boom", "Strict Governance")

    fun advanceWorldState(current: WorldState): Pair<WorldState, MacroDirective> {
        val nextDay = current.inGameDay + 1
        val seasonIndex = ((nextDay / 30) % 4)
        val nextSeason = seasons[seasonIndex]
        val nextYear = current.inGameYear + (if (nextDay % 120 == 0) 1 else 0)

        // Dynamic Weather
        val weatherChance = Random.nextFloat()
        val nextWeather = when {
            nextSeason == "Winter" && weatherChance > 0.6f -> "Blizzard"
            nextSeason == "Summer" && weatherChance > 0.7f -> "Heatwave"
            weatherChance > 0.75f -> "Stormy"
            weatherChance > 0.45f -> "Rainy"
            else -> "Sunny"
        }

        // Economy & Inflation
        val economyRoll = Random.nextFloat()
        val nextTrend = when {
            economyRoll < 0.05f -> "Market Crash"
            economyRoll < 0.25f -> "Recession"
            economyRoll < 0.50f -> "Boom"
            else -> "Stable"
        }

        val inflationDelta = when (nextTrend) {
            "Boom" -> 0.005f
            "Market Crash" -> -0.01f
            "Recession" -> -0.002f
            else -> 0.001f
        }
        val nextInflation = (current.inflationRate + inflationDelta).coerceIn(0.01f, 0.15f)

        // Disaster generation (low probability emergent disaster)
        val disasterRoll = Random.nextFloat()
        val disaster: String? = when {
            disasterRoll < 0.02f -> "Pandemic Outbreak"
            disasterRoll < 0.04f && nextWeather == "Stormy" -> "Flash Flooding"
            disasterRoll < 0.06f && nextWeather == "Heatwave" -> "Wildfire Scourge"
            else -> null
        }

        // Resource Scarcity
        val resourceFactor = when {
            disaster != null -> 0.6f
            nextTrend == "Market Crash" -> 0.7f
            nextTrend == "Boom" -> 1.3f
            else -> 1.0f
        }

        val nextPolitics = if (nextDay % 60 in 0..5) "Election Season" else politicalClimates.random()

        val updatedState = WorldState(
            inGameDay = nextDay,
            inGameSeason = nextSeason,
            inGameYear = nextYear,
            economyTrend = nextTrend,
            inflationRate = nextInflation,
            politicalClimate = nextPolitics,
            weather = nextWeather,
            resourceLevel = resourceFactor,
            activeDisaster = disaster
        )

        val jsonDirective = """
            {
              "inGameDay": $nextDay,
              "season": "$nextSeason",
              "year": $nextYear,
              "economyTrend": "$nextTrend",
              "inflationRate": ${String.format("%.3f", nextInflation)},
              "weather": "$nextWeather",
              "politicalClimate": "$nextPolitics",
              "resourceMultiplier": ${String.format("%.2f", resourceFactor)},
              "activeDisaster": ${if (disaster != null) "\"$disaster\"" else "null"}
            }
        """.trimIndent()

        val directive = MacroDirective(
            day = nextDay,
            economicShift = nextTrend,
            inflationRate = nextInflation,
            weatherCondition = nextWeather,
            politicalDirective = nextPolitics,
            resourceMultiplier = resourceFactor,
            disasterAlert = disaster,
            rawJson = jsonDirective
        )

        return Pair(updatedState, directive)
    }
}
