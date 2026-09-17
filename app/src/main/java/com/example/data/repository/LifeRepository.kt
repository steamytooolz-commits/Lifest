package com.example.data.repository

import com.example.data.local.EventDao
import com.example.data.local.LifeDao
import com.example.data.local.MemoryDao
import com.example.data.local.NpcDao
import com.example.data.model.Event
import com.example.data.model.Life
import com.example.data.model.Memory
import com.example.data.model.NPC
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class LifeRepository(
    private val lifeDao: LifeDao,
    private val npcDao: NpcDao,
    private val memoryDao: MemoryDao,
    private val eventDao: EventDao
) {

    val activeLifeFlow: Flow<Life?> = lifeDao.getActiveLife()
    val allLivesFlow: Flow<List<Life>> = lifeDao.getAllLives()
    val allNpcsFlow: Flow<List<NPC>> = npcDao.getAllNpcs()

    suspend fun getActiveLife(): Life? = lifeDao.getActiveLifeDirect()

    suspend fun getLifeById(id: String): Life? = lifeDao.getLifeById(id)

    suspend fun saveLife(life: Life) {
        lifeDao.insertLife(life)
    }

    suspend fun updateLife(life: Life) {
        lifeDao.updateLife(life)
    }

    suspend fun switchToLife(lifeId: String) {
        lifeDao.deactivateAllLives()
        val target = lifeDao.getLifeById(lifeId)
        if (target != null) {
            lifeDao.updateLife(target.copy(isActive = true))
        }
    }

    suspend fun deleteLife(lifeId: String) {
        lifeDao.deleteLife(lifeId)
        memoryDao.deleteMemoriesForLife(lifeId)
        eventDao.deleteEventsForLife(lifeId)
    }

    suspend fun logEvent(lifeId: String, type: String, description: String, consequences: String = "") {
        val event = Event(
            id = UUID.randomUUID().toString(),
            lifeId = lifeId,
            type = type,
            description = description,
            timestamp = System.currentTimeMillis(),
            consequences = consequences
        )
        eventDao.insertEvent(event)
    }

    fun getEventsFlow(lifeId: String): Flow<List<Event>> = eventDao.getEventsForLife(lifeId)

    suspend fun getEvents(lifeId: String): List<Event> = eventDao.getEventsForLifeDirect(lifeId)

    // NPC and Memory methods
    suspend fun insertNpcs(npcs: List<NPC>) {
        npcDao.insertAll(npcs)
    }

    suspend fun updateNpc(npc: NPC) {
        npcDao.updateNpc(npc)
    }

    suspend fun getNpcById(id: String): NPC? = npcDao.getNpcById(id)

    suspend fun addMemory(memory: Memory, isPaidUser: Boolean) {
        memoryDao.insertMemory(memory)
        if (!isPaidUser) {
            // Free tier: Keep only last 20 memories per NPC
            val memories = memoryDao.getMemoriesForNpcDirect(memory.npcId, memory.lifeId)
            if (memories.size > 20) {
                val excess = memories.size - 20
                memoryDao.trimOldMemories(memory.npcId, excess)
            }
        }
    }

    suspend fun getMemoriesForNpc(npcId: String, lifeId: String): List<Memory> {
        return memoryDao.getMemoriesForNpcDirect(npcId, lifeId)
    }
}
