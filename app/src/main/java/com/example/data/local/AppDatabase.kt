package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AchievementDao
import com.example.data.local.dao.ActivityDao
import com.example.data.local.dao.WorkoutDao
import com.example.data.local.entity.AchievementBadgeEntity
import com.example.data.local.entity.CompletedWorkoutEntity
import com.example.data.local.entity.DailyActivityEntity
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.WorkoutRoutineEntity

@Database(
    entities = [
        WorkoutRoutineEntity::class,
        ExerciseEntity::class,
        CompletedWorkoutEntity::class,
        DailyActivityEntity::class,
        AchievementBadgeEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun activityDao(): ActivityDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pulsefit_fitness_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
