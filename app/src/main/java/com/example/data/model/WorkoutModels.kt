package com.example.data.model

enum class WorkoutCategory(val displayName: String) {
    ALL("All"),
    CHEST("Chest"),
    BACK("Back"),
    LEG("Legs"),
    ARM("Arms"),
    CARDIO("Cardio"),
    FULL_BODY("Full Body"),
    ABS("Abs")
}

enum class WorkoutDifficulty(val displayName: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced")
}

data class Exercise(
    val id: Long = 0,
    val routineId: Long,
    val name: String,
    val targetMuscle: String,
    val sets: Int,
    val reps: Int,
    val durationSeconds: Int = 0, // 0 if rep-based, >0 if timed (e.g. plank)
    val restSeconds: Int = 45,
    val instructions: String,
    val tips: String = "",
    val isCompleted: Boolean = false
)

data class WorkoutRoutine(
    val id: Long = 0,
    val title: String,
    val category: WorkoutCategory,
    val difficulty: WorkoutDifficulty,
    val estimatedDurationMinutes: Int,
    val estimatedCalories: Int,
    val description: String,
    val exerciseCount: Int,
    val exercises: List<Exercise> = emptyList()
)

data class CompletedWorkout(
    val id: Long = 0,
    val routineId: Long,
    val routineTitle: String,
    val category: WorkoutCategory,
    val durationSeconds: Long,
    val caloriesBurned: Int,
    val completedTimestamp: Long,
    val exercisesCompleted: Int
)

data class DailyActivity(
    val date: String, // yyyy-MM-dd
    val steps: Int,
    val stepGoal: Int,
    val caloriesBurned: Int, // Estimated total
    val calorieGoal: Int,
    val activeDurationMinutes: Int,
    val distanceMeters: Double,
    val waterIntakeMl: Int = 0,
    val waterGoalMl: Int = 2500,
    val isDemoData: Boolean = false
) {
    val stepProgress: Float
        get() = if (stepGoal > 0) (steps.toFloat() / stepGoal).coerceIn(0f, 1.5f) else 0f

    val calorieProgress: Float
        get() = if (calorieGoal > 0) (caloriesBurned.toFloat() / calorieGoal).coerceIn(0f, 1.5f) else 0f

    val waterProgress: Float
        get() = if (waterGoalMl > 0) (waterIntakeMl.toFloat() / waterGoalMl).coerceIn(0f, 1.5f) else 0f

    val distanceKm: Double
        get() = distanceMeters / 1000.0
}
