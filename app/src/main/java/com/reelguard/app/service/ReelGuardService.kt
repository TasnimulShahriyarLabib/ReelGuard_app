package com.reelguard.app.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.reelguard.app.BuildConfig
import com.reelguard.app.ReelGuardApp
import com.reelguard.app.data.model.Platform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Blocks short-video feeds.
 *
 * Design notes:
 *  - onAccessibilityEvent does almost nothing: a package check plus scheduling ONE debounced
 *    check ~150 ms later (YouTube fires many content-changed events; they are coalesced).
 *  - The check looks for known view IDs (see [DetectionRules]); no text is ever read.
 *  - Escalation if the feed is still showing: Back -> (500 ms) Home tab / Back -> (500 ms) system Home.
 *  - Events from apps the user hasn't enabled are dropped immediately.
 */
class ReelGuardService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var scope: CoroutineScope? = null

    @Volatile
    private var enabledPackages: Set<String> = emptySet()

    private var checkPending = false
    private var escalationStep = 0
    private var lastRecordUptime = 0L
    private var lastToastUptime = 0L

    private val checkRunnable = Runnable {
        checkPending = false
        runCheck()
    }
    private val recheckRunnable = Runnable { runCheck() }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val container = (application as ReelGuardApp).container
        val newScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        scope = newScope
        if (BuildConfig.DEBUG) {
            DebugDump.trigger = { key, delayMs ->
                handler.postDelayed({ dumpScreen(key) }, delayMs)
            }
        }
        newScope.launch {
            container.settingsRepository.settings.collect { list ->
                enabledPackages = list.asSequence()
                    .filter { it.blockFeed }
                    .mapNotNull { runCatching { Platform.valueOf(it.platform) }.getOrNull() }
                    .flatMap { it.packageNames.asSequence() }
                    .toSet()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg !in enabledPackages) {
            escalationStep = 0 // user left the target app
            return
        }
        if (!checkPending) {
            checkPending = true
            handler.postDelayed(checkRunnable, CHECK_DELAY_MS)
        }
    }

    private fun runCheck() {
        val root = rootInActiveWindow ?: return
        try {
            val pkg = root.packageName?.toString()
            val rule = pkg?.let { DetectionRules.forPackage(it) }
            if (rule == null || pkg !in enabledPackages) {
                escalationStep = 0
                return
            }
            val matchedId = Detector.findFeedMatch(root, rule)
            if (matchedId == null) {
                escalationStep = 0
                return
            }
            if (BuildConfig.DEBUG) Log.d(TAG, "Feed detected: ${rule.platform} via $matchedId (step $escalationStep)")
            handleDetected(rule, root)
        } finally {
            @Suppress("DEPRECATION")
            root.recycle()
        }
    }

    private fun handleDetected(rule: AppRule, root: android.view.accessibility.AccessibilityNodeInfo) {
        val now = SystemClock.uptimeMillis()

        // Count and announce once per episode, not once per escalation attempt.
        if (escalationStep == 0 && now - lastRecordUptime > EPISODE_GAP_MS) {
            lastRecordUptime = now
            val container = (application as ReelGuardApp).container
            container.appScope.launch { container.statsRepository.recordBlock(rule.platform) }
            showToast("${rule.platform.label} blocked")
        }

        when (escalationStep) {
            0 -> performGlobalAction(GLOBAL_ACTION_BACK)
            1 -> if (!Detector.clickHomeTab(root, rule)) performGlobalAction(GLOBAL_ACTION_BACK)
            else -> performGlobalAction(GLOBAL_ACTION_HOME)
        }
        escalationStep++

        handler.removeCallbacks(recheckRunnable)
        handler.postDelayed(recheckRunnable, RECHECK_DELAY_MS)
    }

    private fun showToast(message: String) {
        val now = SystemClock.uptimeMillis()
        if (now - lastToastUptime < TOAST_GAP_MS) return
        lastToastUptime = now
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Debug builds only: records the visible view IDs (and class names) of the foreground
     * app, so the right [DetectionRules] can be found without a computer.
     * Skips anything that isn't one of the supported apps.
     */
    private fun dumpScreen(key: String) {
        val root = rootInActiveWindow
        if (root == null) {
            DebugDump.put(key, DebugDump.DumpResult("", emptyMap(), "no active window"))
            return
        }
        try {
            val pkg = root.packageName?.toString().orEmpty()
            if (Platform.fromPackage(pkg) == null) {
                DebugDump.put(key, DebugDump.DumpResult(pkg, emptyMap(), "skipped: '$pkg' is not a supported app"))
                return
            }
            val ids = LinkedHashMap<String, String>()
            collectIds(root, ids, 0)
            DebugDump.put(key, DebugDump.DumpResult(pkg, ids))
            Toast.makeText(this, "Dump $key saved (${ids.size} IDs)", Toast.LENGTH_SHORT).show()
        } finally {
            @Suppress("DEPRECATION")
            root.recycle()
        }
    }

    @Suppress("DEPRECATION")
    private fun collectIds(node: AccessibilityNodeInfo, out: MutableMap<String, String>, depth: Int) {
        if (depth > 40 || out.size > 800) return
        if (node.isVisibleToUser) {
            node.viewIdResourceName?.let { out.putIfAbsent(it, node.className?.toString().orEmpty()) }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectIds(child, out, depth + 1)
            child.recycle()
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        DebugDump.trigger = null
        handler.removeCallbacksAndMessages(null)
        scope?.cancel()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "ReelGuard"
        const val CHECK_DELAY_MS = 150L
        const val RECHECK_DELAY_MS = 500L
        const val EPISODE_GAP_MS = 2_000L
        const val TOAST_GAP_MS = 3_000L
    }
}
