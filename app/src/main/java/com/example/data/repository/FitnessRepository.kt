package com.example.data.repository

import com.example.data.local.dao.AchievementDao
import com.example.data.local.dao.ActivityDao
import com.example.data.local.dao.WorkoutDao
import com.example.data.local.entity.AchievementBadgeEntity
import com.example.data.local.entity.CompletedWorkoutEntity
import com.example.data.local.entity.DailyActivityEntity
import com.example.data.local.SampleDataProvider
import com.example.data.model.AchievementBadge
import com.example.data.model.CompletedWorkout
import com.example.data.model.DailyActivity
import com.example.data.model.WorkoutCategory
import com.example.data.model.WorkoutRoutine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

class FitnessRepository(
    private val workoutDao: WorkoutDao,
    private val activityDao: ActivityDao,
    private val achievementDao: AchievementDao
) {

    suspend fun initialize() {
        activityDao.deleteDemoActivities()
        SampleDataProvider.populateInitialDataIfEmpty(workoutDao, activityDao, achievementDao)
        evaluateAchievements()
    }

    val allRoutines: Flow<List<WorkoutRoutine>> = workoutDao.getAllRoutines().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getRoutinesByCategory(category: WorkoutCategory): Flow<List<WorkoutRoutine>> {
        return if (category == WorkoutCategory.ALL) {
            allRoutines
        } else {
            workoutDao.getRoutinesByCategory(category.name).map { entities ->
                entities.map { it.toDomain() }
            }
        }
    }

    fun getRoutineWithExercises(routineId: Long): Flow<WorkoutRoutine?> {
        return workoutDao.getExercisesForRoutine(routineId).map { exerciseEntities ->
            val routineEntity = workoutDao.getRoutineById(routineId)
            routineEntity?.toDomain(exerciseEntities.map { it.toDomain() })
        }
    }

    fun getTodayActivity(defaultStepGoal: Int = 10000, defaultCalorieGoal: Int = 600): Flow<DailyActivity> {
        val today = SampleDataProvider.getTodayDateString()
        return activityDao.getActivityForDate(today).map { entity ->
            if (entity != null) {
                entity.toDomain()
            } else {
                // Return default activity for today
                DailyActivity(
                    date = today,
                    steps = 0,
                    stepGoal = defaultStepGoal,
                    caloriesBurned = 0,
                    calorieGoal = defaultCalorieGoal,
                    activeDurationMinutes = 0,
                    distanceMeters = 0.0,
                    waterIntakeMl = 0,
                    waterGoalMl = 2500,
                    isDemoData = false
                )
            }
        }
    }

    fun getRecentDaysActivity(days: Int = 7): Flow<List<DailyActivity>> {
        return activityDao.getRecentDaysActivity(days).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getRecentCompletedWorkouts(limit: Int = 5): Flow<List<CompletedWorkout>> {
        return workoutDao.getRecentCompletedWorkouts(limit).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getAllCompletedWorkouts(): Flow<List<CompletedWorkout>> {
        return workoutDao.getAllCompletedWorkouts().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getCompletedWorkoutsSince(sinceTimestamp: Long): Flow<List<CompletedWorkout>> {
        return workoutDao.getCompletedWorkoutsSince(sinceTimestamp).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun addSteps(stepDelta: Int, stepGoal: Int = 10000, calorieGoal: Int = 600) {
        if (stepDelta <= 0) return
        val today = SampleDataProvider.getTodayDateString()
        val current = activityDao.getActivityForDateSync(today)

        val newSteps = (current?.steps ?: 0) + stepDelta
        val additionalDistance = stepDelta * 0.762 // 0.762m avg stride
        val newDistance = (current?.distanceMeters ?: 0.0) + additionalDistance
        // ~0.045 kcal per walking step
        val additionalCalories = (stepDelta * 0.045).toInt()
        val newCalories = (current?.caloriesBurned ?: 0) + additionalCalories

        val updatedEntity = DailyActivityEntity(
            date = today,
            steps = newSteps,
            stepGoal = current?.stepGoal ?: stepGoal,
            caloriesBurned = newCalories,
            calorieGoal = current?.calorieGoal ?: calorieGoal,
            activeDurationMinutes = current?.activeDurationMinutes ?: 0,
            distanceMeters = newDistance,
            waterIntakeMl = current?.waterIntakeMl ?: 0,
            waterGoalMl = current?.waterGoalMl ?: 2500,
            isDemoData = current?.isDemoData ?: false
        )
        activityDao.insertOrUpdateActivity(updatedEntity)
        evaluateAchievements()
    }

    suspend fun addWaterIntake(amountMl: Int, waterGoal: Int = 2500) {
        val today = SampleDataProvider.getTodayDateString()
        val current = activityDao.getActivityForDateSync(today)
        val newIntake = ((current?.waterIntakeMl ?: 0) + amountMl).coerceAtLeast(0)

        val updatedEntity = DailyActivityEntity(
            date = today,
            steps = current?.steps ?: 0,
            stepGoal = current?.stepGoal ?: 10000,
            caloriesBurned = current?.caloriesBurned ?: 0,
            calorieGoal = current?.calorieGoal ?: 600,
            activeDurationMinutes = current?.activeDurationMinutes ?: 0,
            distanceMeters = current?.distanceMeters ?: 0.0,
            waterIntakeMl = newIntake,
            waterGoalMl = current?.waterGoalMl ?: waterGoal,
            isDemoData = current?.isDemoData ?: false
        )
        activityDao.insertOrUpdateActivity(updatedEntity)
        evaluateAchievements()
    }

    suspend fun recordCompletedWorkout(
        routineId: Long,
        routineTitle: String,
        category: WorkoutCategory,
        durationSeconds: Long,
        caloriesBurned: Int,
        exercisesCompleted: Int,
        stepGoal: Int = 10000,
        calorieGoal: Int = 600
    ) {
        // Save completed workout
        val completedEntity = CompletedWorkoutEntity(
            routineId = routineId,
            routineTitle = routineTitle,
            category = category.name,
            durationSeconds = durationSeconds,
            caloriesBurned = caloriesBurned,
            completedTimestamp = System.currentTimeMillis(),
            exercisesCompleted = exercisesCompleted
        )
        workoutDao.insertCompletedWorkout(completedEntity)

        // Update today's daily activity
        val today = SampleDataProvider.getTodayDateString()
        val current = activityDao.getActivityForDateSync(today)
        val addedMinutes = (durationSeconds / 60).toInt().coerceAtLeast(1)

        val updatedEntity = DailyActivityEntity(
            date = today,
            steps = current?.steps ?: 0,
            stepGoal = current?.stepGoal ?: stepGoal,
            caloriesBurned = (current?.caloriesBurned ?: 0) + caloriesBurned,
            calorieGoal = current?.calorieGoal ?: calorieGoal,
            activeDurationMinutes = (current?.activeDurationMinutes ?: 0) + addedMinutes,
            distanceMeters = current?.distanceMeters ?: 0.0,
            waterIntakeMl = current?.waterIntakeMl ?: 0,
            waterGoalMl = current?.waterGoalMl ?: 2500,
            isDemoData = current?.isDemoData ?: false
        )
        activityDao.insertOrUpdateActivity(updatedEntity)
        evaluateAchievements()
    }

    suspend fun updateDailyGoals(stepGoal: Int, calorieGoal: Int) {
        val today = SampleDataProvider.getTodayDateString()
        val current = activityDao.getActivityForDateSync(today)
        if (current != null) {
            activityDao.insertOrUpdateActivity(
                current.copy(stepGoal = stepGoal, calorieGoal = calorieGoal)
            )
        }
    }

    val allBadges: Flow<List<AchievementBadge>> = achievementDao.getAllBadges().map { list ->
        list.map { it.toDomain() }
    }

    val unlockedBadgeCount: Flow<Int> = achievementDao.getUnlockedBadgeCount()

    suspend fun evaluateAchievements() {
        val completedWorkouts = workoutDao.getAllCompletedWorkoutsSync()
        val allActivities = activityDao.getAllActivitiesSync()
        val totalSteps = activityDao.getTotalStepsSync() ?: 0
        val totalDistanceMeters = activityDao.getTotalDistanceMetersSync() ?: 0.0
        val now = System.currentTimeMillis()

        // 1. Early Bird: Completed workout before 9:00 AM (hour in 4..8)
        val earlyBirdWorkout = completedWorkouts.firstOrNull { workout ->
            val cal = Calendar.getInstance().apply { timeInMillis = workout.completedTimestamp }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hour in 4..8
        }
        val earlyBirdUnlocked = earlyBirdWorkout != null

        // 2. Marathoner: 42.2 km total distance
        val totalDistanceKm = (totalDistanceMeters / 1000.0).toInt()
        val marathonerUnlocked = totalDistanceMeters >= 42195.0

        // 3. Night Owl: Completed workout after 8:00 PM (hour >= 20 or < 4)
        val nightOwlWorkout = completedWorkouts.firstOrNull { workout ->
            val cal = Calendar.getInstance().apply { timeInMillis = workout.completedTimestamp }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hour >= 20 || hour < 4
        }
        val nightOwlUnlocked = nightOwlWorkout != null

        // 4. 10K Club: single day steps >= 10,000
        val maxSingleDaySteps = allActivities.maxOfOrNull { it.steps } ?: 0
        val stepMasterUnlocked = maxSingleDaySteps >= 10000

        // 5. Century Sprint: 50,000 total cumulative steps
        val centurySprintUnlocked = totalSteps >= 50000

        // 6. Hydro Master: single day water >= 2,500 ml
        val maxWater = allActivities.maxOfOrNull { it.waterIntakeMl } ?: 0
        val hydroMasterUnlocked = maxWater >= 2500

        // 7. Calorie Crusher: single day calories >= 500
        val maxCalories = allActivities.maxOfOrNull { it.caloriesBurned } ?: 0
        val calorieCrusherUnlocked = maxCalories >= 500

        // 8. Iron Will: 5 completed workouts
        val workoutCount = completedWorkouts.size
        val ironWillUnlocked = workoutCount >= 5

        val currentBadges = achievementDao.getAllBadgesSync().associateBy { it.id }
        val updatedList = mutableListOf<AchievementBadgeEntity>()

        fun processBadge(
            id: String,
            isUnlocked: Boolean,
            currentValue: Int,
            targetValue: Int,
            timestamp: Long?
        ) {
            val existing = currentBadges[id] ?: return
            val wasUnlocked = existing.isUnlocked
            val newUnlocked = wasUnlocked || isUnlocked
            val newTimestamp = if (newUnlocked) (existing.unlockedTimestamp ?: timestamp ?: now) else null
            val updated = existing.copy(
                isUnlocked = newUnlocked,
                unlockedTimestamp = newTimestamp,
                currentValue = if (newUnlocked) targetValue else currentValue.coerceAtMost(targetValue),
                targetValue = targetValue
            )
            if (updated != existing) {
                updatedList.add(updated)
            }
        }

        processBadge("early_bird", earlyBirdUnlocked, if (earlyBirdUnlocked) 1 else 0, 1, earlyBirdWorkout?.completedTimestamp)
        processBadge("marathoner", marathonerUnlocked, totalDistanceKm, 42, null)
        processBadge("night_owl", nightOwlUnlocked, if (nightOwlUnlocked) 1 else 0, 1, nightOwlWorkout?.completedTimestamp)
        processBadge("step_master", stepMasterUnlocked, maxSingleDaySteps, 10000, null)
        processBadge("century_sprint", centurySprintUnlocked, totalSteps, 50000, null)
        processBadge("hydro_master", hydroMasterUnlocked, maxWater, 2500, null)
        processBadge("calorie_crusher", calorieCrusherUnlocked, maxCalories, 500, null)
        processBadge("iron_will", ironWillUnlocked, workoutCount, 5, null)

        if (updatedList.isNotEmpty()) {
            achievementDao.insertBadges(updatedList)
        }
    }

    suspend fun resetAllData() {
        workoutDao.clearCompletedWorkouts()
        activityDao.clearActivities()
        achievementDao.clearBadges()
        SampleDataProvider.populateInitialDataIfEmpty(workoutDao, activityDao, achievementDao)
        evaluateAchievements()
    }
}
