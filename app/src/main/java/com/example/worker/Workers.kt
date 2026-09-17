package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesRepository
import java.util.concurrent.TimeUnit

class DailyResetWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val userPrefs = UserPreferencesRepository(applicationContext)
            userPrefs.resetDailyLives()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

class PassExpirationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val userPrefs = UserPreferencesRepository(applicationContext)
            userPrefs.checkAndExpirePasses()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

class MemoryConsolidationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val memoryDao = db.memoryDao()
            val npcDao = db.npcDao()
            val lifeDao = db.lifeDao()

            val activeLife = lifeDao.getActiveLifeDirect()
            if (activeLife != null) {
                val npcs = npcDao.getNpcById("dummy") // check connection
            }
            Result.success()
        } catch (e: Exception) {
            Result.success()
        }
    }
}

object WorkManagerScheduler {
    fun scheduleAll(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Daily reset worker every 24 hours
        val dailyResetRequest = PeriodicWorkRequestBuilder<DailyResetWorker>(24, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "DailyResetWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyResetRequest
        )

        // Pass expiration worker every 6 hours
        val passExpirationRequest = PeriodicWorkRequestBuilder<PassExpirationWorker>(6, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "PassExpirationWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            passExpirationRequest
        )

        // Memory consolidation worker every 12 hours
        val memoryConsolidationRequest = PeriodicWorkRequestBuilder<MemoryConsolidationWorker>(12, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "MemoryConsolidationWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            memoryConsolidationRequest
        )
    }
}
