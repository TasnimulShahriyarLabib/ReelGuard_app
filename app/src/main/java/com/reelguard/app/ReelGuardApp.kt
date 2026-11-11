package com.reelguard.app

import android.app.Application
import android.content.Context
import com.reelguard.app.data.db.AppDatabase
import com.reelguard.app.data.prefs.UserPrefs
import com.reelguard.app.data.repo.SettingsRepository
import com.reelguard.app.data.repo.StatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Simple manual dependency container. The AccessibilityService (Step 2) reads the same
 * instances via `(applicationContext as ReelGuardApp).container`.
 */
class AppContainer(context: Context) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val database = AppDatabase.create(context)
    val userPrefs = UserPrefs(context)
    val settingsRepository = SettingsRepository(database.platformSettingsDao())
    val statsRepository = StatsRepository(database.blockEventDao())
}

class ReelGuardApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.appScope.launch {
            container.settingsRepository.ensureDefaults()
            container.statsRepository.prune()
        }
    }
}
