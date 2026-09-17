package com.example.domain.simulation

import com.example.data.model.Life
import com.example.data.model.Stats
import kotlin.random.Random

data class Profession(
    val title: String,
    val category: String,          // "Entry", "Trade", "Tech", "Corporate", "Medical", "Public", "Business"
    val baseSalary: Double,
    val requiredEducation: String, // "None", "High School", "Bachelor", "Master", "PhD"
    val requiredIntellect: Float,
    val stressRating: Float,       // 0..1
    val isPaidTierExclusive: Boolean = false
)

data class InvestmentAsset(
    val id: String,
    val name: String,
    val type: String,              // "Stock", "Crypto", "Real Estate"
    val currentPrice: Double,
    val dailyVolatility: Float,
    val annualYield: Float
)

object CareerEconomyEngine {

    val allProfessions: List<Profession> = listOf(
        // Free Tier (10 professions)
        Profession("Barista", "Entry", 22_000.0, "None", 20f, 0.2f),
        Profession("Retail Associate", "Entry", 24_000.0, "High School", 25f, 0.25f),
        Profession("Delivery Driver", "Entry", 28_000.0, "High School", 25f, 0.3f),
        Profession("Electrician", "Trade", 55_000.0, "High School", 45f, 0.4f),
        Profession("Plumber", "Trade", 52_000.0, "High School", 40f, 0.35f),
        Profession("Junior Software Dev", "Tech", 70_000.0, "Bachelor", 60f, 0.5f),
        Profession("High School Teacher", "Public", 48_000.0, "Bachelor", 55f, 0.45f),
        Profession("Registered Nurse", "Medical", 68_000.0, "Bachelor", 65f, 0.6f),
        Profession("Accountant", "Corporate", 65_000.0, "Bachelor", 60f, 0.4f),
        Profession("Police Officer", "Public", 58_000.0, "High School", 45f, 0.6f),

        // Paid Tier 40+ professions
        Profession("Software Architect", "Tech", 145_000.0, "Bachelor", 80f, 0.55f, isPaidTierExclusive = true),
        Profession("AI Systems Engineer", "Tech", 160_000.0, "Master", 85f, 0.6f, isPaidTierExclusive = true),
        Profession("Game Designer", "Tech", 85_000.0, "Bachelor", 65f, 0.5f, isPaidTierExclusive = true),
        Profession("Data Scientist", "Tech", 120_000.0, "Master", 78f, 0.5f, isPaidTierExclusive = true),
        Profession("Cybersecurity Analyst", "Tech", 110_000.0, "Bachelor", 75f, 0.55f, isPaidTierExclusive = true),
        Profession("UX Director", "Tech", 130_000.0, "Bachelor", 72f, 0.45f, isPaidTierExclusive = true),
        Profession("Investment Banker", "Corporate", 180_000.0, "Master", 80f, 0.85f, isPaidTierExclusive = true),
        Profession("Management Consultant", "Corporate", 140_000.0, "Master", 75f, 0.75f, isPaidTierExclusive = true),
        Profession("Corporate Lawyer", "Corporate", 175_000.0, "Master", 85f, 0.8f, isPaidTierExclusive = true),
        Profession("Financial Analyst", "Corporate", 90_000.0, "Bachelor", 70f, 0.5f, isPaidTierExclusive = true),
        Profession("Chief Executive Officer (CEO)", "Corporate", 350_000.0, "Master", 85f, 0.9f, isPaidTierExclusive = true),
        Profession("Chief Financial Officer (CFO)", "Corporate", 280_000.0, "Master", 85f, 0.8f, isPaidTierExclusive = true),
        Profession("General Physician", "Medical", 210_000.0, "Master", 85f, 0.7f, isPaidTierExclusive = true),
        Profession("Neurosurgeon", "Medical", 450_000.0, "PhD", 95f, 0.95f, isPaidTierExclusive = true),
        Profession("Biochemist", "Medical", 95_000.0, "PhD", 88f, 0.4f, isPaidTierExclusive = true),
        Profession("Clinical Psychologist", "Medical", 105_000.0, "PhD", 82f, 0.5f, isPaidTierExclusive = true),
        Profession("Pharmacist", "Medical", 125_000.0, "Master", 80f, 0.45f, isPaidTierExclusive = true),
        Profession("University Professor", "Public", 115_000.0, "PhD", 85f, 0.35f, isPaidTierExclusive = true),
        Profession("Diplomat", "Public", 135_000.0, "Master", 80f, 0.6f, isPaidTierExclusive = true),
        Profession("Judge", "Public", 195_000.0, "Master", 90f, 0.65f, isPaidTierExclusive = true),
        Profession("City Mayor", "Public", 120_000.0, "Bachelor", 70f, 0.7f, isPaidTierExclusive = true),
        Profession("Senator", "Public", 174_000.0, "Master", 75f, 0.75f, isPaidTierExclusive = true),
        Profession("Astronaut", "Public", 165_000.0, "Master", 92f, 0.85f, isPaidTierExclusive = true),
        Profession("Detective", "Public", 82_000.0, "Bachelor", 68f, 0.65f, isPaidTierExclusive = true),
        Profession("Executive Chef", "Trade", 78_000.0, "High School", 55f, 0.7f, isPaidTierExclusive = true),
        Profession("Master Carpenter", "Trade", 68_000.0, "High School", 50f, 0.4f, isPaidTierExclusive = true),
        Profession("Aviation Mechanic", "Trade", 76_000.0, "High School", 60f, 0.5f, isPaidTierExclusive = true),
        Profession("Commercial Airline Pilot", "Trade", 160_000.0, "Bachelor", 75f, 0.75f, isPaidTierExclusive = true),
        Profession("Fine Artist & Sculptor", "Creative", 55_000.0, "None", 50f, 0.3f, isPaidTierExclusive = true),
        Profession("Music Producer", "Creative", 90_000.0, "None", 60f, 0.5f, isPaidTierExclusive = true),
        Profession("Boutique Tech Studio Owner", "Business", 240_000.0, "Bachelor", 80f, 0.7f, isPaidTierExclusive = true),
        Profession("Restaurant Chain Tycoon", "Business", 320_000.0, "High School", 75f, 0.8f, isPaidTierExclusive = true),
        Profession("Real Estate Conglomerate Owner", "Business", 480_000.0, "Bachelor", 80f, 0.75f, isPaidTierExclusive = true),
        Profession("Renewable Energy Founder", "Business", 400_000.0, "Master", 88f, 0.85f, isPaidTierExclusive = true),
        Profession("Private Equity Partner", "Corporate", 520_000.0, "Master", 90f, 0.9f, isPaidTierExclusive = true),
        Profession("Investigative Journalist", "Creative", 62_000.0, "Bachelor", 70f, 0.6f, isPaidTierExclusive = true),
        Profession("Freelance Consultant", "Creative", 88_000.0, "Bachelor", 65f, 0.45f, isPaidTierExclusive = true),
        Profession("E-Commerce Entrepreneur", "Business", 150_000.0, "None", 65f, 0.65f, isPaidTierExclusive = true),
        Profession("Biomedical Researcher", "Medical", 112_000.0, "PhD", 90f, 0.45f, isPaidTierExclusive = true),
        Profession("Renewable Grid Specialist", "Trade", 82_000.0, "Bachelor", 68f, 0.45f, isPaidTierExclusive = true)
    )

    fun getAvailableProfessions(isPaidUser: Boolean): List<Profession> {
        return if (isPaidUser) allProfessions else allProfessions.filter { !it.isPaidTierExclusive }
    }

    // Default assets
    val defaultAssets = listOf(
        InvestmentAsset("SP500", "Global Index ETF", "Stock", 480.0, 0.015f, 0.08f),
        InvestmentAsset("TECH", "Apex Tech MegaCap", "Stock", 210.0, 0.035f, 0.12f),
        InvestmentAsset("CYBER", "CyberPulse Coin", "Crypto", 65_000.0, 0.085f, 0.25f),
        InvestmentAsset("SOL", "Solaris Token", "Crypto", 145.0, 0.12f, 0.35f),
        InvestmentAsset("RE_REIT", "Metropolitan Real Estate Trust", "Real Estate", 1_250.0, 0.01f, 0.06f)
    )

    /**
     * Simulates stock / crypto price fluctuation
     */
    fun tickAssetPrices(assets: List<InvestmentAsset>, economyTrend: String): List<InvestmentAsset> {
        val trendBias = when (economyTrend) {
            "Boom" -> 0.02f
            "Market Crash" -> -0.08f
            "Recession" -> -0.02f
            else -> 0.005f
        }

        return assets.map { asset ->
            val noise = (Random.nextFloat() - 0.5f) * 2.0f * asset.dailyVolatility
            val pctChange = trendBias + noise
            val newPrice = (asset.currentPrice * (1.0 + pctChange)).coerceAtLeast(1.0)
            asset.copy(currentPrice = newPrice)
        }
    }

    /**
     * Casino Mini-Game: Blackjack Round Simulation
     */
    fun playBlackjack(bet: Double): Pair<Boolean, Double> {
        val playerTotal = Random.nextInt(16, 22)
        val dealerTotal = Random.nextInt(15, 23)

        val won = when {
            dealerTotal > 21 -> true
            playerTotal > dealerTotal -> true
            else -> false
        }
        val returnAmount = if (won) bet * 2.0 else 0.0
        return Pair(won, returnAmount)
    }

    /**
     * Casino Mini-Game: Roulette Round Simulation
     */
    fun playRoulette(bet: Double, pickColor: String): Pair<Boolean, Double> {
        val roll = Random.nextInt(37) // 0..36
        val color = when {
            roll == 0 -> "Green"
            roll % 2 == 0 -> "Red"
            else -> "Black"
        }
        val won = color.equals(pickColor, ignoreCase = true)
        val returnAmount = if (won) bet * 2.0 else 0.0
        return Pair(won, returnAmount)
    }

    /**
     * Casino Mini-Game: Texas Hold'em Hand Simulation
     */
    fun playTexasHoldem(bet: Double): Pair<Boolean, Double> {
        val playerHandRank = Random.nextInt(1, 10) // 1=High Card, 9=Royal Flush
        val opponentHandRank = Random.nextInt(1, 10)
        val won = playerHandRank >= opponentHandRank
        val returnAmount = if (won) bet * 2.5 else 0.0
        return Pair(won, returnAmount)
    }

    /**
     * Tax Calculation & Audit Risk
     */
    fun calculateTaxes(income: Double, taxRate: Float, evadedAmount: Double = 0.0): Pair<Double, Boolean> {
        val taxableIncome = (income - evadedAmount).coerceAtLeast(0.0)
        val owed = taxableIncome * taxRate

        // If evasion occurred, chance of audit proportional to amount evaded
        var wasAudited = false
        if (evadedAmount > 5_000.0) {
            val auditChance = (evadedAmount / income).coerceIn(0.05, 0.70)
            if (Random.nextDouble() < auditChance) {
                wasAudited = true
            }
        }

        return Pair(owed, wasAudited)
    }
}
