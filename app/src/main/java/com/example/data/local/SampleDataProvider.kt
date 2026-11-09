package com.example.data.local

import com.example.data.local.dao.AchievementDao
import com.example.data.local.dao.ActivityDao
import com.example.data.local.dao.WorkoutDao
import com.example.data.local.entity.AchievementBadgeEntity
import com.example.data.local.entity.CompletedWorkoutEntity
import com.example.data.local.entity.DailyActivityEntity
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.WorkoutRoutineEntity
import com.example.data.model.BadgeCategory
import com.example.data.model.WorkoutCategory
import com.example.data.model.WorkoutDifficulty
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SampleDataProvider {

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getDateStringDaysAgo(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }

    suspend fun populateInitialDataIfEmpty(
        workoutDao: WorkoutDao,
        activityDao: ActivityDao,
        achievementDao: AchievementDao
    ) {
        if (workoutDao.getRoutineCount() == 0) {
            seedRoutinesAndExercises(workoutDao)
        }
        initializeTodayActivity(activityDao)
        if (achievementDao.getBadgeCount() == 0) {
            seedInitialAchievements(achievementDao)
        }
    }

    private suspend fun seedRoutinesAndExercises(workoutDao: WorkoutDao) {
        val routines = listOf(
            WorkoutRoutineEntity(
                title = "Chest Sculpt & Power",
                category = WorkoutCategory.CHEST.name,
                difficulty = WorkoutDifficulty.INTERMEDIATE.name,
                estimatedDurationMinutes = 35,
                estimatedCalories = 280,
                description = "Build upper and mid-chest density with compound presses and peak-contraction flyes.",
                exerciseCount = 5
            ),
            WorkoutRoutineEntity(
                title = "V-Taper Back Mastery",
                category = WorkoutCategory.BACK.name,
                difficulty = WorkoutDifficulty.INTERMEDIATE.name,
                estimatedDurationMinutes = 40,
                estimatedCalories = 320,
                description = "Focus on lat width, rhomboid thickness, and lower back stability with pull-ups and rows.",
                exerciseCount = 5
            ),
            WorkoutRoutineEntity(
                title = "Lower Body Thunder",
                category = WorkoutCategory.LEG.name,
                difficulty = WorkoutDifficulty.ADVANCED.name,
                estimatedDurationMinutes = 45,
                estimatedCalories = 410,
                description = "Heavy compound movements targeting quadriceps, hamstrings, glutes, and calves.",
                exerciseCount = 5
            ),
            WorkoutRoutineEntity(
                title = "Bicep & Tricep Pump",
                category = WorkoutCategory.ARM.name,
                difficulty = WorkoutDifficulty.BEGINNER.name,
                estimatedDurationMinutes = 30,
                estimatedCalories = 220,
                description = "Superset isolation routine targeting long and short bicep heads and lateral tricep heads.",
                exerciseCount = 5
            ),
            WorkoutRoutineEntity(
                title = "HIIT Turbo Burn",
                category = WorkoutCategory.CARDIO.name,
                difficulty = WorkoutDifficulty.ADVANCED.name,
                estimatedDurationMinutes = 25,
                estimatedCalories = 350,
                description = "Explosive interval cardio to spike heart rate, accelerate fat oxidation, and boost VO2 max.",
                exerciseCount = 5
            ),
            WorkoutRoutineEntity(
                title = "Total Body Spartan",
                category = WorkoutCategory.FULL_BODY.name,
                difficulty = WorkoutDifficulty.INTERMEDIATE.name,
                estimatedDurationMinutes = 40,
                estimatedCalories = 380,
                description = "Full-body metabolic conditioning connecting upper, lower, and core muscle chains.",
                exerciseCount = 5
            ),
            WorkoutRoutineEntity(
                title = "Core Shred & Six-Pack",
                category = WorkoutCategory.ABS.name,
                difficulty = WorkoutDifficulty.BEGINNER.name,
                estimatedDurationMinutes = 20,
                estimatedCalories = 175,
                description = "Isometric holds and rotational core movements to forge midsection strength and definition.",
                exerciseCount = 5
            )
        )

        val routineIds = workoutDao.insertRoutines(routines)

        val exercises = mutableListOf<ExerciseEntity>()

        // 1. Chest Exercises
        if (routineIds.isNotEmpty()) {
            val chestId = routineIds[0]
            exercises.add(ExerciseEntity(routineId = chestId, name = "Incline Dumbbell Press", targetMuscle = "Upper Chest", sets = 4, reps = 10, durationSeconds = 0, restSeconds = 60, instructions = "Set bench to 30 degrees. Press dumbbells upward with elbows slightly tucked.", tips = "Squeeze upper pectorals at the peak."))
            exercises.add(ExerciseEntity(routineId = chestId, name = "Barbell Flat Bench Press", targetMuscle = "Middle Chest", sets = 4, reps = 8, durationSeconds = 0, restSeconds = 75, instructions = "Grip barbell slightly wider than shoulder-width. Lower smoothly to mid-chest and drive up.", tips = "Keep shoulder blades retracted and feet planted."))
            exercises.add(ExerciseEntity(routineId = chestId, name = "Incline Cable Flyes", targetMuscle = "Inner & Upper Chest", sets = 3, reps = 12, durationSeconds = 0, restSeconds = 45, instructions = "Bring low cables upward in an arcing motion, embracing a wide hug posture.", tips = "Maintain a slight bend at the elbows."))
            exercises.add(ExerciseEntity(routineId = chestId, name = "Parallel Bar Chest Dips", targetMuscle = "Lower Chest & Triceps", sets = 3, reps = 12, durationSeconds = 0, restSeconds = 60, instructions = "Lean torso forward 30 degrees, lower until upper arms are parallel to floor.", tips = "Controlled eccentric movement."))
            exercises.add(ExerciseEntity(routineId = chestId, name = "Decline Push-Ups", targetMuscle = "Upper Chest & Core", sets = 3, reps = 15, durationSeconds = 0, restSeconds = 45, instructions = "Elevate feet on bench or box. Lower chest to floor and push forcefully.", tips = "Engage glutes to prevent hip sag."))
        }

        // 2. Back Exercises
        if (routineIds.size > 1) {
            val backId = routineIds[1]
            exercises.add(ExerciseEntity(routineId = backId, name = "Wide-Grip Pull-Ups", targetMuscle = "Lats & Teres Major", sets = 4, reps = 8, durationSeconds = 0, restSeconds = 75, instructions = "Overhand grip. Pull chest toward the bar, driving elbows down and back.", tips = "Pause 1 second at the top."))
            exercises.add(ExerciseEntity(routineId = backId, name = "Bent-Over Barbell Row", targetMuscle = "Mid Back & Rhomboids", sets = 4, reps = 10, durationSeconds = 0, restSeconds = 60, instructions = "Hinge hips back at 45 degrees. Pull bar toward lower ribs.", tips = "Keep spine neutral throughout."))
            exercises.add(ExerciseEntity(routineId = backId, name = "Close-Grip Lat Pulldown", targetMuscle = "Lats", sets = 3, reps = 12, durationSeconds = 0, restSeconds = 45, instructions = "Use V-bar handle. Pull downward smoothly to collarbone level.", tips = "Avoid swinging torso backward."))
            exercises.add(ExerciseEntity(routineId = backId, name = "Single-Arm Dumbbell Row", targetMuscle = "Lats & Upper Back", sets = 3, reps = 12, durationSeconds = 0, restSeconds = 45, instructions = "Place knee and hand on flat bench. Row dumbbell up toward hip pocket.", tips = "Lead with the elbow, not the wrist."))
            exercises.add(ExerciseEntity(routineId = backId, name = "Face Pulls with Rope", targetMuscle = "Rear Delts & Rotators", sets = 3, reps = 15, durationSeconds = 0, restSeconds = 45, instructions = "Set cable high. Pull rope ends toward ears, flaring elbows out.", tips = "Key for postural balance."))
        }

        // 3. Leg Exercises
        if (routineIds.size > 2) {
            val legId = routineIds[2]
            exercises.add(ExerciseEntity(routineId = legId, name = "Barbell Back Squats", targetMuscle = "Quadriceps & Glutes", sets = 4, reps = 8, durationSeconds = 0, restSeconds = 90, instructions = "Rest bar on upper traps. Squat down until thighs break parallel.", tips = "Drive knees outward in line with toes."))
            exercises.add(ExerciseEntity(routineId = legId, name = "Romanian Deadlifts", targetMuscle = "Hamstrings & Glutes", sets = 4, reps = 10, durationSeconds = 0, restSeconds = 75, instructions = "Hold barbell or dumbbells. Push hips back with slight knee bend until hamstrings stretch.", tips = "Keep weight close to shins."))
            exercises.add(ExerciseEntity(routineId = legId, name = "Bulgarian Split Squats", targetMuscle = "Quads & Stabilizers", sets = 3, reps = 10, durationSeconds = 0, restSeconds = 60, instructions = "Back foot on bench. Lower rear knee toward floor in vertical motion.", tips = "Keep torso tall for quad emphasis."))
            exercises.add(ExerciseEntity(routineId = legId, name = "Walking Dumbbell Lunges", targetMuscle = "Legs & Core", sets = 3, reps = 16, durationSeconds = 0, restSeconds = 60, instructions = "Step forward firmly, dropping back knee an inch above the floor.", tips = "Maintain upright posture."))
            exercises.add(ExerciseEntity(routineId = legId, name = "Standing Calf Raises", targetMuscle = "Gastrocnemius", sets = 4, reps = 15, durationSeconds = 0, restSeconds = 45, instructions = "Elevate toes on ledge. Rise onto balls of feet, hold peak 2 seconds.", tips = "Full stretch at bottom."))
        }

        // 4. Arm Exercises
        if (routineIds.size > 3) {
            val armId = routineIds[3]
            exercises.add(ExerciseEntity(routineId = armId, name = "Standing Barbell Bicep Curl", targetMuscle = "Biceps", sets = 4, reps = 10, durationSeconds = 0, restSeconds = 60, instructions = "Shoulder-width grip. Curl bar upward without swinging shoulders.", tips = "Lock elbows at your ribs."))
            exercises.add(ExerciseEntity(routineId = armId, name = "Overhead Tricep Extension", targetMuscle = "Triceps Long Head", sets = 4, reps = 12, durationSeconds = 0, restSeconds = 60, instructions = "Hold dumbbell overhead with both hands. Lower behind head and extend.", tips = "Keep elbows pointed forward."))
            exercises.add(ExerciseEntity(routineId = armId, name = "Incline Dumbbell Hammer Curls", targetMuscle = "Brachialis & Forearms", sets = 3, reps = 12, durationSeconds = 0, restSeconds = 45, instructions = "Neutral palms-facing grip. Curl smoothly on an incline bench.", tips = "Develops arm thickness."))
            exercises.add(ExerciseEntity(routineId = armId, name = "Tricep Bench Dips", targetMuscle = "Triceps", sets = 3, reps = 15, durationSeconds = 0, restSeconds = 45, instructions = "Hands on bench edge behind you. Lower hips until elbows hit 90 degrees.", tips = "Add plate on lap for extra resistance."))
            exercises.add(ExerciseEntity(routineId = armId, name = "Cable Rope Pushdowns", targetMuscle = "Triceps Lateral Head", sets = 3, reps = 15, durationSeconds = 0, restSeconds = 45, instructions = "Push rope down and spread ends outward at full extension.", tips = "Squeeze triceps at lock-out."))
        }

        // 5. Cardio Exercises
        if (routineIds.size > 4) {
            val cardioId = routineIds[4]
            exercises.add(ExerciseEntity(routineId = cardioId, name = "High-Intensity Burpees", targetMuscle = "Full Body & Cardio", sets = 4, reps = 15, durationSeconds = 0, restSeconds = 45, instructions = "Drop down to pushup position, kick feet back, perform pushup, leap upward.", tips = "Land softly on balls of feet."))
            exercises.add(ExerciseEntity(routineId = cardioId, name = "Speed Mountain Climbers", targetMuscle = "Core & Hip Flexors", sets = 4, reps = 0, durationSeconds = 40, restSeconds = 30, instructions = "High plank position. Rapidly drive alternating knees toward chest.", tips = "Maintain a flat stable back."))
            exercises.add(ExerciseEntity(routineId = cardioId, name = "Jump Rope Double Unders", targetMuscle = "Calves & Shoulders", sets = 4, reps = 0, durationSeconds = 45, restSeconds = 30, instructions = "Rhythmic jumps turning rope fast, landing cleanly.", tips = "Turn rope using wrists, not whole arms."))
            exercises.add(ExerciseEntity(routineId = cardioId, name = "Fast Lateral Skaters", targetMuscle = "Glutes & Agility", sets = 3, reps = 20, durationSeconds = 0, restSeconds = 30, instructions = "Leap sideways from foot to foot in an explosive skater motion.", tips = "Absorb landing into a deep knee bend."))
            exercises.add(ExerciseEntity(routineId = cardioId, name = "High Knee Sprint in Place", targetMuscle = "Hip Flexors & Cardio", sets = 3, reps = 0, durationSeconds = 45, restSeconds = 30, instructions = "Sprint on the spot bringing knees above waist level.", tips = "Pump arms vigorously with each stride."))
        }

        // 6. Full Body Exercises
        if (routineIds.size > 5) {
            val fullBodyId = routineIds[5]
            exercises.add(ExerciseEntity(routineId = fullBodyId, name = "Kettlebell Swings", targetMuscle = "Hamstrings, Glutes & Core", sets = 4, reps = 15, durationSeconds = 0, restSeconds = 60, instructions = "Hinge hips backward and snap forcefully forward to swing bell to chest height.", tips = "Power originates from hip extension."))
            exercises.add(ExerciseEntity(routineId = fullBodyId, name = "Dumbbell Thrusters", targetMuscle = "Quads, Shoulders & Triceps", sets = 4, reps = 10, durationSeconds = 0, restSeconds = 60, instructions = "Front squat dumbbells down, then explosively drive up overhead in one motion.", tips = "Use leg drive to propel weights up."))
            exercises.add(ExerciseEntity(routineId = fullBodyId, name = "Renegade Rows with Dumbbells", targetMuscle = "Back, Shoulders & Core", sets = 3, reps = 12, durationSeconds = 0, restSeconds = 60, instructions = "Plank on dumbbells. Alternate rowing each dumbbell without tilting hips.", tips = "Widen feet for stability."))
            exercises.add(ExerciseEntity(routineId = fullBodyId, name = "Farmer's Walk", targetMuscle = "Traps, Grip & Core", sets = 3, reps = 0, durationSeconds = 45, restSeconds = 45, instructions = "Carry heavy dumbbells at your sides with proud posture and slow steps.", tips = "Keep shoulders pinned back."))
            exercises.add(ExerciseEntity(routineId = fullBodyId, name = "Bear Crawl Matrix", targetMuscle = "Shoulders & Core", sets = 3, reps = 0, durationSeconds = 40, restSeconds = 45, instructions = "Knees hovering 2 inches off floor. Crawl forward and backward rhythmically.", tips = "Keep back as flat as a table."))
        }

        // 7. Abs Exercises
        if (routineIds.size > 6) {
            val absId = routineIds[6]
            exercises.add(ExerciseEntity(routineId = absId, name = "Forearm Plank Hold", targetMuscle = "Transverse Abdominis", sets = 3, reps = 0, durationSeconds = 60, restSeconds = 45, instructions = "Rest on forearms and toes. Form a rigid straight line from heels to crown.", tips = "Pull navel in toward spine."))
            exercises.add(ExerciseEntity(routineId = absId, name = "Russian Twists with Weight", targetMuscle = "Internal & External Obliques", sets = 3, reps = 20, durationSeconds = 0, restSeconds = 30, instructions = "Sit with torso angled at 45 degrees, rotate plate or dumbbell side to side.", tips = "Rotate from ribs, not just arms."))
            exercises.add(ExerciseEntity(routineId = absId, name = "Hanging Knee-to-Chest Raises", targetMuscle = "Lower Abs & Hip Flexors", sets = 3, reps = 12, durationSeconds = 0, restSeconds = 45, instructions = "Hang from pullup bar. Curl knees toward chest, lifting pelvis.", tips = "Avoid swinging momentum."))
            exercises.add(ExerciseEntity(routineId = absId, name = "Bicycle Crunches", targetMuscle = "Obliques & Rectus Abdominis", sets = 3, reps = 20, durationSeconds = 0, restSeconds = 30, instructions = "Touch opposite elbow to opposite knee while cycling legs.", tips = "Keep lower back pressed against mat."))
            exercises.add(ExerciseEntity(routineId = absId, name = "Dead Bug Core Stabilization", targetMuscle = "Deep Core & Pelvic Floor", sets = 3, reps = 16, durationSeconds = 0, restSeconds = 30, instructions = "Lie on back. Lower opposite arm and leg simultaneously without arching back.", tips = "Breathe out as limbs extend."))
        }

        workoutDao.insertExercises(exercises)
    }

    private suspend fun initializeTodayActivity(activityDao: ActivityDao) {
        val today = getTodayDateString()
        if (activityDao.getActivityForDateSync(today) == null) {
            val todayActivity = DailyActivityEntity(
                date = today,
                steps = 0,
                stepGoal = 10000,
                caloriesBurned = 0,
                calorieGoal = 600,
                activeDurationMinutes = 0,
                distanceMeters = 0.0,
                waterIntakeMl = 0,
                waterGoalMl = 2500,
                isDemoData = false
            )
            activityDao.insertOrUpdateActivity(todayActivity)
        }
    }

    suspend fun seedInitialAchievements(achievementDao: AchievementDao) {
        val badges = listOf(
            AchievementBadgeEntity(
                id = "early_bird",
                title = "Early Bird",
                description = "Complete a workout session before 9:00 AM.",
                category = BadgeCategory.WORKOUT.name,
                iconKey = "early_bird",
                accentColorHex = "#CCFF00",
                isUnlocked = false,
                unlockedTimestamp = null,
                currentValue = 0,
                targetValue = 1,
                unit = "workout"
            ),
            AchievementBadgeEntity(
                id = "marathoner",
                title = "Marathoner",
                description = "Accumulate 42.2 km of total stride distance.",
                category = BadgeCategory.DISTANCE.name,
                iconKey = "marathoner",
                accentColorHex = "#00E5FF",
                isUnlocked = false,
                unlockedTimestamp = null,
                currentValue = 0,
                targetValue = 42,
                unit = "km"
            ),
            AchievementBadgeEntity(
                id = "night_owl",
                title = "Night Owl",
                description = "Finish a training session after 8:00 PM.",
                category = BadgeCategory.WORKOUT.name,
                iconKey = "night_owl",
                accentColorHex = "#A855F7",
                isUnlocked = false,
                unlockedTimestamp = null,
                currentValue = 0,
                targetValue = 1,
                unit = "workout"
            ),
            AchievementBadgeEntity(
                id = "step_master",
                title = "10K Club",
                description = "Conquer 10,000 steps in a single day.",
                category = BadgeCategory.STEPS.name,
                iconKey = "step_master",
                accentColorHex = "#CCFF00",
                isUnlocked = false,
                unlockedTimestamp = null,
                currentValue = 0,
                targetValue = 10000,
                unit = "steps"
            ),
            AchievementBadgeEntity(
                id = "century_sprint",
                title = "Century Sprint",
                description = "Reach 50,000 total cumulative steps across your journey.",
                category = BadgeCategory.STEPS.name,
                iconKey = "century_sprint",
                accentColorHex = "#FFD700",
                isUnlocked = false,
                unlockedTimestamp = null,
                currentValue = 0,
                targetValue = 50000,
                unit = "steps"
            ),
            AchievementBadgeEntity(
                id = "hydro_master",
                title = "Hydro Master",
                description = "Hit your daily water intake goal of 2,500 ml.",
                category = BadgeCategory.HYDRATION.name,
                iconKey = "hydro_master",
                accentColorHex = "#00E5FF",
                isUnlocked = false,
                unlockedTimestamp = null,
                currentValue = 0,
                targetValue = 2500,
                unit = "ml"
            ),
            AchievementBadgeEntity(
                id = "calorie_crusher",
                title = "Calorie Crusher",
                description = "Burn over 500 estimated kcal in a single day.",
                category = BadgeCategory.CALORIES.name,
                iconKey = "calorie_crusher",
                accentColorHex = "#FF6B00",
                isUnlocked = false,
                unlockedTimestamp = null,
                currentValue = 0,
                targetValue = 500,
                unit = "kcal"
            ),
            AchievementBadgeEntity(
                id = "iron_will",
                title = "Iron Will",
                description = "Complete 5 workout training routines.",
                category = BadgeCategory.WORKOUT.name,
                iconKey = "iron_will",
                accentColorHex = "#FF2A55",
                isUnlocked = false,
                unlockedTimestamp = null,
                currentValue = 0,
                targetValue = 5,
                unit = "workouts"
            )
        )
        achievementDao.insertBadges(badges)
    }
}

