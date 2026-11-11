package com.reelguard.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Per-platform user settings. One row per [com.reelguard.app.data.model.Platform],
 * keyed by the enum name (e.g. "YOUTUBE").
 *
 * Minutes are "minutes after local midnight" (22:00 = 1320).
 */
@Entity(tableName = "platform_settings")
data class PlatformSettings(
    @PrimaryKey val platform: String,
    /** Block the short-video feed (Reels/Shorts/...) inside this app. Off until the user opts in. */
    val blockFeed: Boolean = false,
    /** Block the whole app during the schedule window. */
    val scheduleEnabled: Boolean = false,
    val scheduleStartMinute: Int = 22 * 60,
    val scheduleEndMinute: Int = 6 * 60,
    /** Daily usage limit in minutes. 0 = no limit. */
    val dailyLimitMinutes: Int = 0,
) {
    /** True if [minuteOfDay] falls inside the schedule window. Handles windows that wrap past midnight. */
    fun isScheduleActive(minuteOfDay: Int): Boolean {
        if (!scheduleEnabled) return false
        return if (scheduleStartMinute <= scheduleEndMinute) {
            minuteOfDay in scheduleStartMinute until scheduleEndMinute
        } else {
            minuteOfDay >= scheduleStartMinute || minuteOfDay < scheduleEndMinute
        }
    }
}
