package com.example

import android.app.Application
import android.os.Build
import android.webkit.WebView
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.remote.FirestoreCouponService
import com.example.data.remote.PuterBridge
import com.example.data.repository.AiRepository
import com.example.data.repository.BillingRepository
import com.example.data.repository.LifeRepository
import com.example.worker.WorkManagerScheduler

class AppContainer(private val application: Application) {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(application)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(application)
    }

    val firestoreCouponService: FirestoreCouponService by lazy {
        FirestoreCouponService()
    }

    val puterBridge: PuterBridge by lazy {
        PuterBridge(application)
    }

    val lifeRepository: LifeRepository by lazy {
        LifeRepository(
            lifeDao = database.lifeDao(),
            npcDao = database.npcDao(),
            memoryDao = database.memoryDao(),
            eventDao = database.eventDao()
        )
    }

    val billingRepository: BillingRepository by lazy {
        BillingRepository(
            preferencesRepository = userPreferencesRepository,
            firestoreCouponService = firestoreCouponService
        )
    }

    val aiRepository: AiRepository by lazy {
        AiRepository(
            puterBridge = puterBridge,
            lifeRepository = lifeRepository
        )
    }
}

class AiLifeSimApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        // Configure system properties to prevent headless / containerized MESA rendernode queries
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val processName = getProcessName()
                if (processName != null && processName.isNotEmpty() && !processName.endsWith(":")) {
                    try {
                        WebView.setDataDirectorySuffix(processName)
                    } catch (e: Exception) {
                        // Already initialized or default
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore for non-standard environments
        }

        container = AppContainer(this)
        try {
            WorkManagerScheduler.scheduleAll(this)
        } catch (e: Exception) {
            // WorkManager may not be initialized in isolated unit test environments
        }
    }
}
