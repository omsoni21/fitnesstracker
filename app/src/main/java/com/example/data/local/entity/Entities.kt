package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.CompletedWorkout
import com.example.data.model.DailyActivity
import com.example.data.model.Exercise
import com.example.data.model.WorkoutCategory
import com.example.data.model.WorkoutDifficulty
import com.example.data.model.WorkoutRoutine

@Entity(tableName = "workout_routines")
data class WorkoutRoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // WorkoutCategory name
    val difficulty: String, // WorkoutDifficulty name
    val estimatedDurationMinutes: Int,
    val estimatedCalories: Int,
    val description: String,
    val exerciseCount: Int
) {
    fun toDomain(exercises: List<Exercise> = emptyList()) = WorkoutRoutine(
        id = id,
        title = title,
        category = runCatching { WorkoutCategory.valueOf(category) }.getOrDefault(WorkoutCategory.FULL_BODY),
        difficulty = runCatching { WorkoutDifficulty.valueOf(difficulty) }.getOrDefault(WorkoutDifficulty.INTERMEDIATE),
        estimatedDurationMinutes = estimatedDurationMinutes,
        estimatedCalories = estimatedCalories,
        description = description,
        exerciseCount = exerciseCount,
        exercises = exercises
    )
}

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutRoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["routineId"])]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val name: String,
    val targetMuscle: String,
    val sets: Int,
    val reps: Int,
    val durationSeconds: Int,
    val restSeconds: Int,
    val instructions: String,
    val tips: String
) {
    fun toDomain() = Exercise(
        id = id,
        routineId = routineId,
        name = name,
        targetMuscle = targetMuscle,
        sets = sets,
        reps = reps,
        durationSeconds = durationSeconds,
        restSeconds = restSeconds,
        instructions = instructions,
        tips = tips
    )
}

@Entity(tableName = "completed_workouts")
data class CompletedWorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val routineTitle: String,
    val category: String,
    val durationSeconds: Long,
    val caloriesBurned: Int,
    val completedTimestamp: Long,
    val exercisesCompleted: Int
) {
    fun toDomain() = CompletedWorkout(
        id = id,
        routineId = routineId,
        routineTitle = routineTitle,
        category = runCatching { WorkoutCategory.valueOf(category) }.getOrDefault(WorkoutCategory.FULL_BODY),
        durationSeconds = durationSeconds,
        caloriesBurned = caloriesBurned,
        completedTimestamp = completedTimestamp,
        exercisesCompleted = exercisesCompleted
    )
}

@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    @PrimaryKey
    val date: String, // yyyy-MM-dd
    val steps: Int,
    val stepGoal: Int,
    val caloriesBurned: Int,
    val calorieGoal: Int,
    val activeDurationMinutes: Int,
    val distanceMeters: Double,
    val waterIntakeMl: Int = 0,
    val waterGoalMl: Int = 2500,
    val isDemoData: Boolean = false
) {
    fun toDomain() = DailyActivity(
        date = date,
        steps = steps,
        stepGoal = stepGoal,
        caloriesBurned = caloriesBurned,
        calorieGoal = calorieGoal,
        activeDurationMinutes = activeDurationMinutes,
        distanceMeters = distanceMeters,
        waterIntakeMl = waterIntakeMl,
        waterGoalMl = waterGoalMl,
        isDemoData = isDemoData
    )
}

@Entity(tableName = "achievement_badges")
data class AchievementBadgeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String, // BadgeCategory name
    val iconKey: String,
    val accentColorHex: String,
    val isUnlocked: Boolean = false,
    val unlockedTimestamp: Long? = null,
    val currentValue: Int = 0,
    val targetValue: Int = 1,
    val unit: String = ""
) {
    fun toDomain() = com.example.data.model.AchievementBadge(
        id = id,
        title = title,
        description = description,
        category = runCatching { com.example.data.model.BadgeCategory.valueOf(category) }
            .getOrDefault(com.example.data.model.BadgeCategory.WORKOUT),
        iconKey = iconKey,
        accentColorHex = accentColorHex,
        isUnlocked = isUnlocked,
        unlockedTimestamp = unlockedTimestamp,
        currentValue = currentValue,
        targetValue = targetValue,
        unit = unit
    )

    companion object {
        fun fromDomain(badge: com.example.data.model.AchievementBadge) = AchievementBadgeEntity(
            id = badge.id,
            title = badge.title,
            description = badge.description,
            category = badge.category.name,
            iconKey = badge.iconKey,
            accentColorHex = badge.accentColorHex,
            isUnlocked = badge.isUnlocked,
            unlockedTimestamp = badge.unlockedTimestamp,
            currentValue = badge.currentValue,
            targetValue = badge.targetValue,
            unit = badge.unit
        )
    }
}

