package com.reelguard.app.data.repo

import com.reelguard.app.data.db.BlockEvent
import com.reelguard.app.data.db.BlockEventDao
import com.reelguard.app.data.db.DayCount
import com.reelguard.app.data.db.PlatformCount
import com.reelguard.app.data.db.PlatformSettings
import com.reelguard.app.data.db.PlatformSettingsDao
import com.reelguard.app.data.model.Platform
import com.reelguard.app.util.epochDayOf
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val dao: PlatformSettingsDao) {

    val settings: Flow<List<PlatformSettings>> = dao.observeAll()

    suspend fun getAll(): List<PlatformSettings> = dao.getAll()

    /** Creates a default row for every platform that doesn't have one yet. */
    suspend fun ensureDefaults() {
        dao.insertAllIfAbsent(Platform.entries.map { PlatformSettings(platform = it.name) })
    }

    suspend fun setBlockFeed(platform: Platform, enabled: Boolean) {
        val current = dao.get(platform.name) ?: PlatformSettings(platform = platform.name)
        dao.upsert(current.copy(blockFeed = enabled))
    }

    suspend fun update(settings: PlatformSettings) = dao.upsert(settings)
}

class StatsRepository(
    private val dao: BlockEventDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend fun recordBlock(platform: Platform) {
        val now = clock()
        dao.insert(BlockEvent(platform = platform.name, timestampMillis = now, dayEpoch = epochDayOf(now)))
    }

    /** Counts per day for the last [days] days including today. */
    fun dailyCounts(days: Int): Flow<List<DayCount>> =
        dao.observeDailyCounts(epochDayOf(clock()) - (days - 1))

    fun platformCounts(days: Int): Flow<List<PlatformCount>> =
        dao.observePlatformCounts(epochDayOf(clock()) - (days - 1))

    /** Housekeeping: keep only the last [keepDays] days of events. */
    suspend fun prune(keepDays: Int = 90) {
        dao.deleteOlderThan(epochDayOf(clock()) - keepDays)
    }
}
