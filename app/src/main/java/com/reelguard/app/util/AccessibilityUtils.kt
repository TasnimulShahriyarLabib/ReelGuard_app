package com.reelguard.app.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.reelguard.app.service.ReelGuardService

/** True if the user has switched ReelGuard on in Android's Accessibility settings. */
fun isReelGuardServiceEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false
    val self = ComponentName(context, ReelGuardService::class.java)
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == self }
}
