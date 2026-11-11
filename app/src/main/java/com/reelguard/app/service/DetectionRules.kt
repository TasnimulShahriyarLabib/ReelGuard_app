package com.reelguard.app.service

import com.reelguard.app.data.model.Platform

/**
 * Detection rules for ONE app. All app-specific knowledge lives here so it can be
 * updated in one place when an app changes its UI.
 *
 * @property feedViewIds full view IDs ("<package>:id/<name>") that exist ONLY on the
 *   full-screen short-video player. If any one is on screen, we treat it as the feed.
 * @property homeTabViewIds view IDs of the app's "Home" tab button, used as the
 *   second escalation step. Empty = skip that step (falls back to Back again).
 */
data class AppRule(
    val platform: Platform,
    val packageName: String,
    val feedViewIds: List<String>,
    val homeTabViewIds: List<String> = emptyList(),
)

object DetectionRules {

    // IMPORTANT: these IDs are best-known candidates and are NOT verified against the
    // current YouTube build. Confirm them on your phone (see README / Step 2 notes):
    //   adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml
    // Dump once while a Short is playing and once on the normal Home feed. Keep only IDs
    // that appear in the first dump and NOT in the second.
    val rules: List<AppRule> = listOf(
        AppRule(
            platform = Platform.YOUTUBE,
            packageName = "com.google.android.youtube",
            feedViewIds = listOf(
                "com.google.android.youtube:id/reel_player_page_container",
                "com.google.android.youtube:id/reel_recycler",
                "com.google.android.youtube:id/reel_watch_fragment_root",
            ),
            homeTabViewIds = emptyList(), // fill after checking the dump (the bottom-bar Home tab)
        ),
    )

    private val byPackage: Map<String, AppRule> = rules.associateBy { it.packageName }

    fun forPackage(packageName: String): AppRule? = byPackage[packageName]
}
