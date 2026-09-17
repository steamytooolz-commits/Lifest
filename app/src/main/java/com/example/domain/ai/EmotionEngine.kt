package com.example.domain.ai

import com.example.data.model.Emotion
import com.example.data.model.OCEAN
import kotlin.math.atan2
import kotlin.math.sqrt

object EmotionEngine {

    /**
     * Russell's Circumplex Model (2D Valence-Arousal Space)
     * Valence: -1.0 (Unhappy/Unpleasant) to +1.0 (Happy/Pleasant)
     * Arousal:  0.0 (Deactivated/Low energy) to 1.0 (Activated/High energy)
     */
    fun getCircumplexLabel(valence: Float, arousal: Float): String {
        return when {
            valence >= 0.2f && arousal >= 0.5f -> when {
                valence > 0.6f && arousal > 0.7f -> "Euphoric"
                valence > 0.5f -> "Joyful"
                else -> "Excited"
            }
            valence >= 0.2f && arousal < 0.5f -> when {
                valence > 0.6f -> "Serene"
                else -> "Content"
            }
            valence < -0.2f && arousal >= 0.5f -> when {
                valence < -0.6f && arousal > 0.7f -> "Furious"
                arousal > 0.7f -> "Terrified"
                else -> "Frustrated"
            }
            valence < -0.2f && arousal < 0.5f -> when {
                valence < -0.6f -> "Depressed"
                arousal < 0.25f -> "Apathetic"
                else -> "Sad"
            }
            else -> when {
                arousal > 0.7f -> "Alert"
                arousal < 0.3f -> "Relaxed"
                else -> "Neutral"
            }
        }
    }

    /**
     * Modulates emotional reaction based on OCEAN traits:
     * - Openness: Higher arousal to novel inputs
     * - Conscientiousness: Resists extreme valence drops, stabilizes arousal
     * - Extraversion: Boosts positive valence, increases social arousal
     * - Agreeableness: Reduces hostility/anger, increases empathy
     * - Neuroticism: Amplifies negative valence shifts and increases stress arousal
     */
    fun calculateEmotionalResponse(
        currentEmotion: Emotion,
        rawValenceDelta: Float,
        rawArousalDelta: Float,
        ocean: OCEAN
    ): Emotion {
        var valenceDelta = rawValenceDelta
        var arousalDelta = rawArousalDelta

        // Extraversion amplifies positive events
        if (rawValenceDelta > 0) {
            valenceDelta *= (0.7f + ocean.extraversion * 0.6f)
        }

        // Neuroticism amplifies negative events and arousal
        if (rawValenceDelta < 0) {
            valenceDelta *= (0.7f + ocean.neuroticism * 0.8f)
            arousalDelta += (ocean.neuroticism * 0.2f)
        }

        // Agreeableness softens anger/negative reactions
        if (rawValenceDelta < 0) {
            valenceDelta *= (1.2f - ocean.agreeableness * 0.4f)
        }

        // Conscientiousness stabilizes emotional swings
        val stabilityFactor = 1.0f - (ocean.conscientiousness * 0.3f)
        valenceDelta *= stabilityFactor
        arousalDelta *= stabilityFactor

        val newValence = (currentEmotion.valence + valenceDelta).coerceIn(-1.0f, 1.0f)
        val newArousal = (currentEmotion.arousal + arousalDelta).coerceIn(0.0f, 1.0f)
        val label = getCircumplexLabel(newValence, newArousal)
        val intensity = (sqrt((newValence * newValence) + (newArousal * newArousal)) / 1.414f).coerceIn(0.0f, 1.0f)

        return Emotion(
            valence = newValence,
            arousal = newArousal,
            label = label,
            intensity = intensity
        )
    }

    /**
     * Emotion Contagion through social contact:
     * Emotion bleeds partially to nearby NPCs based on Extraversion & Agreeableness.
     */
    fun simulateContagion(target: Emotion, source: Emotion, targetOcean: OCEAN): Emotion {
        val contagionRate = 0.15f * targetOcean.agreeableness
        val newV = (target.valence * (1f - contagionRate) + source.valence * contagionRate).coerceIn(-1f, 1f)
        val newA = (target.arousal * (1f - contagionRate) + source.arousal * contagionRate).coerceIn(0f, 1f)
        return Emotion(
            valence = newV,
            arousal = newA,
            label = getCircumplexLabel(newV, newA),
            intensity = target.intensity
        )
    }
}
