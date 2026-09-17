package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Event
import com.example.data.model.Life
import com.example.data.model.Memory
import com.example.data.model.NPC
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeDao {
    @Query("SELECT * FROM lives WHERE isActive = 1 LIMIT 1")
    fun getActiveLife(): Flow<Life?>

    @Query("SELECT * FROM lives WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveLifeDirect(): Life?

    @Query("SELECT * FROM lives ORDER BY createdAt DESC")
    fun getAllLives(): Flow<List<Life>>

    @Query("SELECT * FROM lives WHERE id = :id")
    suspend fun getLifeById(id: String): Life?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLife(life: Life)

    @Update
    suspend fun updateLife(life: Life)

    @Query("UPDATE lives SET isActive = 0")
    suspend fun deactivateAllLives()

    @Query("DELETE FROM lives WHERE id = :id")
    suspend fun deleteLife(id: String)
}

@Dao
interface NpcDao {
    @Query("SELECT * FROM npcs")
    fun getAllNpcs(): Flow<List<NPC>>

    @Query("SELECT * FROM npcs WHERE id = :id")
    suspend fun getNpcById(id: String): NPC?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNpc(npc: NPC)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(npcs: List<NPC>)

    @Update
    suspend fun updateNpc(npc: NPC)

    @Query("DELETE FROM npcs")
    suspend fun clearNpcs()
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE npcId = :npcId AND lifeId = :lifeId ORDER BY timestamp DESC")
    fun getMemoriesForNpc(npcId: String, lifeId: String): Flow<List<Memory>>

    @Query("SELECT * FROM memories WHERE npcId = :npcId AND lifeId = :lifeId ORDER BY timestamp DESC")
    suspend fun getMemoriesForNpcDirect(npcId: String, lifeId: String): List<Memory>

    @Query("SELECT * FROM memories WHERE lifeId = :lifeId ORDER BY timestamp DESC")
    suspend fun getAllMemoriesForLife(lifeId: String): List<Memory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: Memory)

    @Query("DELETE FROM memories WHERE lifeId = :lifeId")
    suspend fun deleteMemoriesForLife(lifeId: String)

    @Query("DELETE FROM memories WHERE id IN (SELECT id FROM memories WHERE npcId = :npcId ORDER BY timestamp ASC LIMIT :count)")
    suspend fun trimOldMemories(npcId: String, count: Int)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE lifeId = :lifeId ORDER BY timestamp DESC")
    fun getEventsForLife(lifeId: String): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE lifeId = :lifeId ORDER BY timestamp DESC")
    suspend fun getEventsForLifeDirect(lifeId: String): List<Event>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Query("DELETE FROM events WHERE lifeId = :lifeId")
    suspend fun deleteEventsForLife(lifeId: String)
}
