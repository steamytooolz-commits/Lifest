package com.example.domain.simulation

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class CountryData(
    val code: String,
    val name: String,
    val region: String,
    val currency: String,
    val costOfLivingIndex: Float, // 1.0 = baseline
    val taxRate: Float,           // e.g. 0.20 = 20%
    val safetyIndex: Float,       // 0..100
    val healthcareQuality: Float, // 0..100
    val freedomIndex: Float,      // 0..100
    val isFreeTier: Boolean = false
)

object WorldGenerator {

    // 25 Free Tier Countries + selection of 195 full list
    val allCountries: List<CountryData> = listOf(
        // Free tier 25 countries
        CountryData("US", "United States", "North America", "$", 1.2f, 0.22f, 75f, 85f, 88f, isFreeTier = true),
        CountryData("GB", "United Kingdom", "Europe", "£", 1.15f, 0.25f, 82f, 88f, 90f, isFreeTier = true),
        CountryData("CA", "Canada", "North America", "C$", 1.1f, 0.26f, 88f, 90f, 92f, isFreeTier = true),
        CountryData("DE", "Germany", "Europe", "€", 1.05f, 0.35f, 86f, 92f, 91f, isFreeTier = true),
        CountryData("FR", "France", "Europe", "€", 1.08f, 0.32f, 80f, 91f, 89f, isFreeTier = true),
        CountryData("JP", "Japan", "Asia", "¥", 1.12f, 0.24f, 95f, 95f, 85f, isFreeTier = true),
        CountryData("AU", "Australia", "Oceania", "A$", 1.18f, 0.27f, 87f, 91f, 92f, isFreeTier = true),
        CountryData("BR", "Brazil", "South America", "R$", 0.65f, 0.18f, 60f, 65f, 78f, isFreeTier = true),
        CountryData("IN", "India", "Asia", "₹", 0.45f, 0.15f, 68f, 62f, 74f, isFreeTier = true),
        CountryData("MX", "Mexico", "North America", "Mex$", 0.6f, 0.19f, 58f, 68f, 75f, isFreeTier = true),
        CountryData("KR", "South Korea", "Asia", "₩", 1.05f, 0.22f, 90f, 93f, 86f, isFreeTier = true),
        CountryData("IT", "Italy", "Europe", "€", 0.98f, 0.30f, 79f, 87f, 85f, isFreeTier = true),
        CountryData("ES", "Spain", "Europe", "€", 0.92f, 0.28f, 84f, 89f, 88f, isFreeTier = true),
        CountryData("NL", "Netherlands", "Europe", "€", 1.15f, 0.36f, 89f, 93f, 94f, isFreeTier = true),
        CountryData("SE", "Sweden", "Europe", "kr", 1.2f, 0.40f, 90f, 94f, 96f, isFreeTier = true),
        CountryData("CH", "Switzerland", "Europe", "CHF", 1.6f, 0.20f, 96f, 96f, 95f, isFreeTier = true),
        CountryData("NZ", "New Zealand", "Oceania", "NZ$", 1.12f, 0.25f, 91f, 90f, 95f, isFreeTier = true),
        CountryData("ZA", "South Africa", "Africa", "R", 0.55f, 0.22f, 50f, 64f, 76f, isFreeTier = true),
        CountryData("EG", "Egypt", "Africa", "E£", 0.4f, 0.16f, 62f, 58f, 55f, isFreeTier = true),
        CountryData("NG", "Nigeria", "Africa", "₦", 0.42f, 0.14f, 52f, 50f, 60f, isFreeTier = true),
        CountryData("AR", "Argentina", "South America", "AR$", 0.5f, 0.25f, 66f, 72f, 80f, isFreeTier = true),
        CountryData("CL", "Chile", "South America", "CLP$", 0.75f, 0.20f, 78f, 80f, 84f, isFreeTier = true),
        CountryData("ID", "Indonesia", "Asia", "Rp", 0.48f, 0.15f, 72f, 60f, 70f, isFreeTier = true),
        CountryData("TH", "Thailand", "Asia", "฿", 0.52f, 0.16f, 74f, 75f, 65f, isFreeTier = true),
        CountryData("SG", "Singapore", "Asia", "S$", 1.35f, 0.17f, 97f, 95f, 78f, isFreeTier = true),

        // Paid Tier bonus countries (sample of the remaining 170)
        CountryData("NO", "Norway", "Europe", "kr", 1.3f, 0.38f, 93f, 95f, 97f),
        CountryData("FI", "Finland", "Europe", "€", 1.15f, 0.36f, 94f, 95f, 98f),
        CountryData("DK", "Denmark", "Europe", "kr", 1.25f, 0.42f, 92f, 94f, 96f),
        CountryData("IE", "Ireland", "Europe", "€", 1.18f, 0.28f, 88f, 88f, 92f),
        CountryData("AT", "Austria", "Europe", "€", 1.08f, 0.34f, 90f, 92f, 91f),
        CountryData("BE", "Belgium", "Europe", "€", 1.06f, 0.37f, 83f, 91f, 89f),
        CountryData("PT", "Portugal", "Europe", "€", 0.85f, 0.26f, 88f, 84f, 89f),
        CountryData("GR", "Greece", "Europe", "€", 0.80f, 0.27f, 76f, 80f, 82f),
        CountryData("PL", "Poland", "Europe", "zł", 0.72f, 0.23f, 82f, 78f, 79f),
        CountryData("CZ", "Czech Republic", "Europe", "Kč", 0.78f, 0.22f, 85f, 83f, 84f),
        CountryData("HU", "Hungary", "Europe", "Ft", 0.68f, 0.24f, 78f, 74f, 70f),
        CountryData("RO", "Romania", "Europe", "lei", 0.62f, 0.20f, 75f, 70f, 75f),
        CountryData("UA", "Ukraine", "Europe", "₴", 0.45f, 0.18f, 55f, 65f, 68f),
        CountryData("TR", "Turkey", "Eurasia", "₺", 0.50f, 0.22f, 65f, 74f, 60f),
        CountryData("AE", "United Arab Emirates", "Middle East", "AED", 1.25f, 0.05f, 92f, 87f, 65f),
        CountryData("SA", "Saudi Arabia", "Middle East", "SAR", 0.95f, 0.10f, 85f, 80f, 55f),
        CountryData("IL", "Israel", "Middle East", "₪", 1.20f, 0.28f, 80f, 90f, 82f),
        CountryData("MY", "Malaysia", "Asia", "RM", 0.54f, 0.17f, 77f, 78f, 72f),
        CountryData("PH", "Philippines", "Asia", "₱", 0.46f, 0.18f, 64f, 60f, 70f),
        CountryData("VN", "Vietnam", "Asia", "₫", 0.44f, 0.15f, 78f, 67f, 58f),
        CountryData("IS", "Iceland", "Europe", "kr", 1.35f, 0.35f, 97f, 93f, 95f),
        CountryData("LU", "Luxembourg", "Europe", "€", 1.30f, 0.30f, 93f, 94f, 94f),
        CountryData("MC", "Monaco", "Europe", "€", 2.20f, 0.00f, 98f, 95f, 85f),
        CountryData("KE", "Kenya", "Africa", "KSh", 0.48f, 0.18f, 58f, 55f, 68f),
        CountryData("MA", "Morocco", "Africa", "MAD", 0.52f, 0.20f, 70f, 62f, 64f),
        CountryData("CO", "Colombia", "South America", "COP$", 0.52f, 0.21f, 60f, 68f, 76f),
        CountryData("PE", "Peru", "South America", "S/", 0.55f, 0.19f, 64f, 66f, 74f)
    )

    fun getCountriesForUser(isPaidUser: Boolean): List<CountryData> {
        return if (isPaidUser) allCountries else allCountries.filter { it.isFreeTier }
    }

    /**
     * Procedural Perlin noise-based terrain generation simulation
     */
    fun samplePerlinNoiseTerrain(seed: Long, x: Float, y: Float): TerrainBio {
        val n = (sin(x * 0.15f + seed % 100) * cos(y * 0.15f + (seed / 100) % 100) + 1.0f) * 0.5f
        return when {
            n > 0.75f -> TerrainBio("Alpine Mountain", "Rugged peaks with mineral deposits and clean mountain air.", 0.85f)
            n > 0.50f -> TerrainBio("Rolling Hills", "Temperate fertile valleys suitable for farming and expanding towns.", 1.1f)
            n > 0.25f -> TerrainBio("Coastal Basin", "Thriving trade ports, marine ecology, and pleasant sea breezes.", 1.25f)
            else -> TerrainBio("Forest Lowlands", "Dense woodlands teeming with wildlife, timber, and secluded cabins.", 1.0f)
        }
    }
}

data class TerrainBio(
    val name: String,
    val description: String,
    val resourceBonus: Float
)
