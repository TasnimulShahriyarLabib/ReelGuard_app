package com.reelguard.app.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.reelguard.app.util.isReelGuardServiceEnabled

/** Shows whether the blocking service is on, and a shortcut to Accessibility settings. */
@Composable
fun ServiceStatusCard() {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(isReelGuardServiceEnabled(context)) }

    // Re-check every time the user comes back from the settings screen.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        enabled = isReelGuardServiceEnabled(context)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = if (enabled) "Blocking service: ON" else "Blocking service: OFF",
            style = MaterialTheme.typography.titleMedium,
        )
        if (!enabled) {
            Spacer(Modifier.height(8.dp))
            Text("Turn on ReelGuard under Installed apps / Downloaded apps in Accessibility settings.")
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                context.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }) { Text("Open Accessibility settings") }
        }
    }
    HorizontalDivider()
}
