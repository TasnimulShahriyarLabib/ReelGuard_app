package com.reelguard.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlatformSettingsDao {
    @Query("SELECT * FROM platform_settings")
    fun observeAll(): Flow<List<PlatformSettings>>

    @Query("SELECT * FROM platform_settings")
    suspend fun getAll(): List<PlatformSettings>

    @Query("SELECT * FROM platform_settings WHERE platform = :platform")
    suspend fun get(platform: String): PlatformSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: PlatformSettings)

    /** Used to seed default rows without overwriting anything the user already changed. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIfAbsent(settings: List<PlatformSettings>)
}

@Dao
interface BlockEventDao {
    @Insert
    suspend fun insert(event: BlockEvent)

    @Query(
        "SELECT dayEpoch, COUNT(*) AS count FROM block_events " +
            "WHERE dayEpoch >= :fromDay GROUP BY dayEpoch ORDER BY dayEpoch"
    )
    fun observeDailyCounts(fromDay: Long): Flow<List<DayCount>>

    @Query(
        "SELECT platform, COUNT(*) AS count FROM block_events " +
            "WHERE dayEpoch >= :fromDay GROUP BY platform"
    )
    fun observePlatformCounts(fromDay: Long): Flow<List<PlatformCount>>

    @Query("DELETE FROM block_events WHERE dayEpoch < :beforeDay")
    suspend fun deleteOlderThan(beforeDay: Long)
}
