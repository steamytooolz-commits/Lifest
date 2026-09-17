package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.Emotion
import com.example.data.model.Item
import com.example.data.model.OCEAN
import com.example.data.model.Relationship
import com.example.data.model.Stats
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val relationshipListType = Types.newParameterizedType(List::class.java, Relationship::class.java)
    private val relationshipListAdapter = moshi.adapter<List<Relationship>>(relationshipListType)

    private val itemListType = Types.newParameterizedType(List::class.java, Item::class.java)
    private val itemListAdapter = moshi.adapter<List<Item>>(itemListType)

    private val statsAdapter = moshi.adapter(Stats::class.java)
    private val oceanAdapter = moshi.adapter(OCEAN::class.java)
    private val emotionAdapter = moshi.adapter(Emotion::class.java)

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return stringListAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            stringListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromRelationshipList(list: List<Relationship>?): String {
        return relationshipListAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toRelationshipList(json: String?): List<Relationship> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            relationshipListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromItemList(list: List<Item>?): String {
        return itemListAdapter.toJson(list ?: emptyList())
    }

    @TypeConverter
    fun toItemList(json: String?): List<Item> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            itemListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStats(stats: Stats?): String {
        return statsAdapter.toJson(stats ?: Stats())
    }

    @TypeConverter
    fun toStats(json: String?): Stats {
        if (json.isNullOrBlank()) return Stats()
        return try {
            statsAdapter.fromJson(json) ?: Stats()
        } catch (e: Exception) {
            Stats()
        }
    }

    @TypeConverter
    fun fromOCEAN(ocean: OCEAN?): String {
        return oceanAdapter.toJson(ocean ?: OCEAN())
    }

    @TypeConverter
    fun toOCEAN(json: String?): OCEAN {
        if (json.isNullOrBlank()) return OCEAN()
        return try {
            oceanAdapter.fromJson(json) ?: OCEAN()
        } catch (e: Exception) {
            OCEAN()
        }
    }

    @TypeConverter
    fun fromEmotion(emotion: Emotion?): String {
        return emotionAdapter.toJson(emotion ?: Emotion())
    }

    @TypeConverter
    fun toEmotion(json: String?): Emotion {
        if (json.isNullOrBlank()) return Emotion()
        return try {
            emotionAdapter.fromJson(json) ?: Emotion()
        } catch (e: Exception) {
            Emotion()
        }
    }
}
