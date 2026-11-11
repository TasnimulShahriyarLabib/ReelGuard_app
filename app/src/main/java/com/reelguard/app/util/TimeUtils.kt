package com.reelguard.app.util

import java.time.Instant
import java.time.ZoneId

/** Local calendar day for [millis], as epoch-day. */
fun epochDayOf(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().toEpochDay()

/** Minutes after local midnight for [millis]. */
fun minuteOfDayOf(millis: Long, zone: ZoneId = ZoneId.systemDefault()): Int {
    val t = Instant.ofEpochMilli(millis).atZone(zone).toLocalTime()
    return t.hour * 60 + t.minute
}
