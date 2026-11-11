package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DailyActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM daily_activity WHERE date = :date")
    fun getActivityForDate(date: String): Flow<DailyActivityEntity?>

    @Query("SELECT * FROM daily_activity WHERE date = :date")
    suspend fun getActivityForDateSync(date: String): DailyActivityEntity?

    @Query("SELECT * FROM daily_activity ORDER BY date DESC LIMIT :days")
    fun getRecentDaysActivity(days: Int): Flow<List<DailyActivityEntity>>

    @Query("SELECT * FROM daily_activity WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getActivitiesBetween(startDate: String, endDate: String): Flow<List<DailyActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateActivity(activity: DailyActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<DailyActivityEntity>)

    @Query("SELECT COUNT(*) FROM daily_activity")
    suspend fun getActivityCount(): Int

    @Query("SELECT * FROM daily_activity ORDER BY date DESC")
    suspend fun getAllActivitiesSync(): List<DailyActivityEntity>

    @Query("SELECT SUM(steps) FROM daily_activity")
    suspend fun getTotalStepsSync(): Int?

    @Query("SELECT SUM(distanceMeters) FROM daily_activity")
    suspend fun getTotalDistanceMetersSync(): Double?

    @Query("DELETE FROM daily_activity WHERE isDemoData = 1")
    suspend fun deleteDemoActivities()

    @Query("DELETE FROM daily_activity")
    suspend fun clearActivities()
}
