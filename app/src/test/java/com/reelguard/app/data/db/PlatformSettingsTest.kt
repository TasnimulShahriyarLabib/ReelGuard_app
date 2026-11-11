package com.reelguard.app.data.db

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlatformSettingsTest {

    private fun s(start: Int, end: Int, enabled: Boolean = true) =
        PlatformSettings(
            platform = "FACEBOOK",
            scheduleEnabled = enabled,
            scheduleStartMinute = start,
            scheduleEndMinute = end,
        )

    @Test
    fun disabledScheduleIsNeverActive() {
        assertFalse(s(22 * 60, 6 * 60, enabled = false).isScheduleActive(23 * 60))
    }

    @Test
    fun wrapAroundWindow_22to6() {
        val x = s(22 * 60, 6 * 60)
        assertTrue(x.isScheduleActive(23 * 60))      // 23:00 -> blocked
        assertTrue(x.isScheduleActive(2 * 60))       // 02:00 -> blocked
        assertTrue(x.isScheduleActive(22 * 60))      // 22:00 start is inclusive
        assertFalse(x.isScheduleActive(6 * 60))      // 06:00 end is exclusive
        assertFalse(x.isScheduleActive(12 * 60))     // noon -> allowed
    }

    @Test
    fun sameDayWindow_9to17() {
        val x = s(9 * 60, 17 * 60)
        assertTrue(x.isScheduleActive(9 * 60))
        assertTrue(x.isScheduleActive(16 * 60 + 59))
        assertFalse(x.isScheduleActive(17 * 60))
        assertFalse(x.isScheduleActive(8 * 60))
    }

    @Test
    fun emptyWindow_startEqualsEnd_neverActive() {
        assertFalse(s(10 * 60, 10 * 60).isScheduleActive(10 * 60))
    }
}
