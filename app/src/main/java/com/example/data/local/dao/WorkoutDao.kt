package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.CompletedWorkoutEntity
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.WorkoutRoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_routines ORDER BY id ASC")
    fun getAllRoutines(): Flow<List<WorkoutRoutineEntity>>

    @Query("SELECT * FROM workout_routines WHERE category = :category ORDER BY id ASC")
    fun getRoutinesByCategory(category: String): Flow<List<WorkoutRoutineEntity>>

    @Query("SELECT * FROM workout_routines WHERE id = :routineId")
    suspend fun getRoutineById(routineId: Long): WorkoutRoutineEntity?

    @Query("SELECT * FROM exercises WHERE routineId = :routineId ORDER BY id ASC")
    fun getExercisesForRoutine(routineId: Long): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE routineId = :routineId ORDER BY id ASC")
    suspend fun getExercisesForRoutineSync(routineId: Long): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<WorkoutRoutineEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedWorkout(completed: CompletedWorkoutEntity): Long

    @Query("SELECT * FROM completed_workouts ORDER BY completedTimestamp DESC")
    fun getAllCompletedWorkouts(): Flow<List<CompletedWorkoutEntity>>

    @Query("SELECT * FROM completed_workouts ORDER BY completedTimestamp DESC LIMIT :limit")
    fun getRecentCompletedWorkouts(limit: Int): Flow<List<CompletedWorkoutEntity>>

    @Query("SELECT * FROM completed_workouts WHERE completedTimestamp >= :sinceTimestamp ORDER BY completedTimestamp DESC")
    fun getCompletedWorkoutsSince(sinceTimestamp: Long): Flow<List<CompletedWorkoutEntity>>

    @Query("SELECT COUNT(*) FROM workout_routines")
    suspend fun getRoutineCount(): Int

    @Query("SELECT * FROM completed_workouts")
    suspend fun getAllCompletedWorkoutsSync(): List<CompletedWorkoutEntity>

    @Query("SELECT COUNT(*) FROM completed_workouts")
    suspend fun getCompletedWorkoutCount(): Int

    @Query("DELETE FROM completed_workouts")
    suspend fun clearCompletedWorkouts()
}
