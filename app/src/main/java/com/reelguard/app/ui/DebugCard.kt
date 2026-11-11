package com.reelguard.app.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reelguard.app.service.DebugDump

/**
 * Debug builds only. Lets you find the view IDs that exist on the Shorts screen but not on
 * the normal YouTube home screen, using just the phone:
 *   1. Tap "A", open YouTube Home within 8 s and stay there.
 *   2. Tap "B", open a Short within 8 s and stay there.
 *   3. The IDs that appear only in B are listed below. Copy them and send them over.
 */
@Composable
fun DebugCard() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val results by DebugDump.results.collectAsStateWithLifecycle()
    val a = results["A"]
    val b = results["B"]

    val onlyInB: List<String> =
        if (a != null && b != null && a.note == null && b.note == null) {
            b.ids.filterKeys { it !in a.ids }.map { (id, cls) -> "$id  ($cls)" }
        } else emptyList()

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Debug: find view IDs", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Tap a button, then within 8 seconds open YouTube and stay on that screen. " +
                "Do A on the YouTube Home feed and B while a Short is playing."
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { start(context, "A") }) { Text("A: Home feed") }
            Button(onClick = { start(context, "B") }) { Text("B: Short") }
            OutlinedButton(onClick = { DebugDump.clear() }) { Text("Clear") }
        }
        Spacer(Modifier.height(8.dp))

        a?.let { Text("A: " + (it.note ?: "${it.pkg}, ${it.ids.size} IDs")) }
        b?.let { Text("B: " + (it.note ?: "${it.pkg}, ${it.ids.size} IDs")) }

        if (a != null && b != null) {
            Spacer(Modifier.height(8.dp))
            Text("Only on the Short screen (${onlyInB.size}):", style = MaterialTheme.typography.titleSmall)
            SelectionContainer {
                Text(onlyInB.joinToString("\n").ifEmpty { "(none yet)" })
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                val full = buildString {
                    appendLine("ONLY IN B:")
                    onlyInB.forEach { appendLine(it) }
                    appendLine()
                    appendLine("ALL IN B:")
                    b.ids.forEach { (id, cls) -> appendLine("$id  ($cls)") }
                }
                clipboard.setText(AnnotatedString(full))
                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
            }) { Text("Copy result") }
        }
    }
    HorizontalDivider()
}

private fun start(context: Context, key: String) {
    val trigger = DebugDump.trigger
    if (trigger == null) {
        Toast.makeText(context, "Turn on the Accessibility service first", Toast.LENGTH_LONG).show()
        return
    }
    trigger(key, 8_000L)
    Toast.makeText(context, "Open YouTube now (8 s)", Toast.LENGTH_LONG).show()
}
