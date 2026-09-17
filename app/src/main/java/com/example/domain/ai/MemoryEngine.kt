package com.example.domain.ai

import com.example.data.model.Memory
import kotlin.math.ln
import kotlin.math.max

object MemoryEngine {

    /**
     * Retrieves Top-K memories ranked by:
     * - Relevance: Keyword & tag matching score against query
     * - Recency: Time decay factor
     * - Importance: Intrinsic importance assigned during memory creation
     */
    fun retrieveTopKMemories(
        query: String,
        memories: List<Memory>,
        topK: Int = 5
    ): List<Memory> {
        if (memories.isEmpty()) return emptyList()

        val queryTokens = tokenize(query)
        val now = System.currentTimeMillis()
        val oneDayMs = 24L * 60 * 60 * 1000

        val scored = memories.map { memory ->
            val memoryTokens = tokenize(memory.content) + memory.tags.flatMap { tokenize(it) }
            val relevance = calculateRelevance(queryTokens, memoryTokens)

            // Recency decay: Halves roughly every 7 days
            val ageDays = max(0.0, (now - memory.timestamp).toDouble() / oneDayMs)
            val recency = (1.0 / (1.0 + (0.1 * ageDays))).toFloat()

            val importance = memory.importance.coerceIn(0f, 1f)

            val totalScore = (relevance * 0.55f) + (recency * 0.30f) + (importance * 0.15f)
            Pair(memory, totalScore)
        }

        return scored.sortedByDescending { it.second }
            .take(topK)
            .map { it.first }
    }

    private fun tokenize(text: String): Set<String> {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split("\\s+".toRegex())
            .filter { it.length > 2 }
            .toSet()
    }

    private fun calculateRelevance(queryTokens: Set<String>, docTokens: Collection<String>): Float {
        if (queryTokens.isEmpty() || docTokens.isEmpty()) return 0.1f

        var matches = 0
        for (q in queryTokens) {
            if (docTokens.contains(q)) {
                matches++
            }
        }
        return (matches.toFloat() / queryTokens.size.toFloat()).coerceIn(0.1f, 1.0f)
    }

    /**
     * Formats memories into an LLM prompt context block
     */
    fun formatMemoriesForPrompt(memories: List<Memory>): String {
        if (memories.isEmpty()) return "None"
        return memories.joinToString("\n") { m ->
            "- [V:${String.format("%.1f", m.valence)}, A:${String.format("%.1f", m.arousal)}] ${m.content} (tags: ${m.tags.joinToString(", ")})"
        }
    }
}
