package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AchievementBadgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievement_badges ORDER BY isUnlocked DESC, id ASC")
    fun getAllBadges(): Flow<List<AchievementBadgeEntity>>

    @Query("SELECT * FROM achievement_badges ORDER BY isUnlocked DESC, id ASC")
    suspend fun getAllBadgesSync(): List<AchievementBadgeEntity>

    @Query("SELECT * FROM achievement_badges WHERE id = :id")
    suspend fun getBadgeById(id: String): AchievementBadgeEntity?

    @Query("SELECT COUNT(*) FROM achievement_badges WHERE isUnlocked = 1")
    fun getUnlockedBadgeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM achievement_badges")
    suspend fun getBadgeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<AchievementBadgeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBadge(badge: AchievementBadgeEntity)

    @Query("DELETE FROM achievement_badges")
    suspend fun clearBadges()
}
