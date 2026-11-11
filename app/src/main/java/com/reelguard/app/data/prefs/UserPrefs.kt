package com.reelguard.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "reelguard_prefs")

/** Small app-wide preferences (not per-platform; those live in Room). */
class UserPrefs(context: Context) {
    private val store = context.applicationContext.dataStore

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        /** Epoch millis until which strict mode is locked. 0 = strict mode off. (Enforced in Step 4.) */
        val STRICT_UNTIL = longPreferencesKey("strict_until_millis")
    }

    val onboardingDone: Flow<Boolean> = store.data.map { it[Keys.ONBOARDING_DONE] ?: false }
    val strictUntilMillis: Flow<Long> = store.data.map { it[Keys.STRICT_UNTIL] ?: 0L }

    suspend fun setOnboardingDone(done: Boolean) {
        store.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setStrictUntil(millis: Long) {
        store.edit { it[Keys.STRICT_UNTIL] = millis }
    }
}
