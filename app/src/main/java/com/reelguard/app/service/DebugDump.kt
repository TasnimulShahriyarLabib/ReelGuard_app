package com.reelguard.app.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Debug-build helper for finding the right view IDs without a computer.
 * Holds the results of "dump the on-screen view IDs" requests. Only view ID names and
 * class names are ever collected — never text, descriptions or passwords.
 */
object DebugDump {

    data class DumpResult(
        val pkg: String,
        /** view ID -> class name, for views currently visible on screen. */
        val ids: Map<String, String>,
        /** Set when the dump failed or was skipped. */
        val note: String? = null,
    )

    /** Registered by [ReelGuardService] while it is connected (debug builds only). */
    @Volatile
    var trigger: ((key: String, delayMs: Long) -> Unit)? = null

    private val _results = MutableStateFlow<Map<String, DumpResult>>(emptyMap())
    val results: StateFlow<Map<String, DumpResult>> = _results

    fun put(key: String, result: DumpResult) {
        _results.update { it + (key to result) }
    }

    fun clear() {
        _results.value = emptyMap()
    }
}
