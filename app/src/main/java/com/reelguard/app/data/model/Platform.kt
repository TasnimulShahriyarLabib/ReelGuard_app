package com.reelguard.app.data.model

/**
 * The short-video features ReelGuard can block.
 * Package names are only used to decide which apps' events we process.
 * (Step 3 moves the per-app detection rules into one config file.)
 */
enum class Platform(val label: String, val packageNames: Set<String>) {
    FACEBOOK("Facebook Reels", setOf("com.facebook.katana")),
    INSTAGRAM("Instagram Reels", setOf("com.instagram.android")),
    YOUTUBE("YouTube Shorts", setOf("com.google.android.youtube")),
    TIKTOK("TikTok feed", setOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")),
    SNAPCHAT("Snapchat Spotlight", setOf("com.snapchat.android"));

    companion object {
        fun fromPackage(packageName: String): Platform? =
            entries.firstOrNull { packageName in it.packageNames }
    }
}
