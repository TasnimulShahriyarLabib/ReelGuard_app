package com.reelguard.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One "we blocked a Reels/Shorts screen" event. Stores ONLY which platform and when —
 * never any screen content.
 */
@Entity(tableName = "block_events", indices = [Index("dayEpoch")])
data class BlockEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val platform: String,
    val timestampMillis: Long,
    /** Local date as epoch-day, for cheap per-day grouping. */
    val dayEpoch: Long,
)

data class DayCount(val dayEpoch: Long, val count: Int)

data class PlatformCount(val platform: String, val count: Int)
