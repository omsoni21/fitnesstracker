package com.example.util

import android.content.Context
import android.content.Intent

object ShareHelper {

    fun shareCompletedWorkout(
        context: Context,
        routineTitle: String,
        category: String,
        durationSeconds: Long,
        caloriesBurned: Int,
        exercisesCompleted: Int
    ) {
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        val durationFormatted = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"

        val shareText = buildString {
            appendLine("⚡ WORKOUT COMPLETED WITH MM FITNESS ⚡")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🏋️ Routine: $routineTitle")
            appendLine("🎯 Target: $category")
            appendLine("⏱️ Duration: $durationFormatted")
            appendLine("🔥 Estimated Burn: $caloriesBurned kcal")
            appendLine("✅ Exercises Finished: $exercisesCompleted")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Pushed my limits with MM Fitness — Native on-device workout & fitness tracking.")
            appendLine("#MMFitness #FitnessGoals #WorkoutDone #FitnessTracker")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Crushed my $routineTitle workout!")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Workout Summary")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun shareWeeklyReport(
        context: Context,
        totalSteps: Int,
        totalCalories: Int,
        totalDurationMinutes: Int,
        mostCompletedCategory: String,
        stepsComparisonPercent: Float
    ) {
        val shareText = buildString {
            appendLine("📊 MY MM FITNESS WEEKLY REPORT 📊")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("👟 Total Steps: ${"%,d".format(totalSteps)} steps (+${stepsComparisonPercent}% vs last week)")
            appendLine("🔥 Total Calories Burned: ${"%,d".format(totalCalories)} kcal (est.)")
            appendLine("⏱️ Active Workout Time: $totalDurationMinutes minutes")
            appendLine("🏆 Most Completed Focus: $mostCompletedCategory")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Building daily momentum and healthy habits with MM Fitness.")
            appendLine("#MMFitness #WeeklyReport #FitnessProgress #StepCounter")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Weekly Fitness Report on MM Fitness")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Weekly Report")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun shareRoutine(
        context: Context,
        title: String,
        category: String,
        durationMinutes: Int,
        calories: Int,
        exerciseCount: Int
    ) {
        val shareText = buildString {
            appendLine("💪 CHECK OUT THIS WORKOUT ON MM FITNESS 💪")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🏋️ Routine: $title")
            appendLine("🎯 Category: $category")
            appendLine("⏱️ Est. Time: $durationMinutes mins")
            appendLine("🔥 Est. Calories: $calories kcal")
            appendLine("📋 Exercise Count: $exerciseCount exercises")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Join me for this workout routine in MM Fitness!")
            appendLine("#MMFitness #WorkoutRoutine #Fitness")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Try this $title workout!")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Workout Routine")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun shareAchievement(
        context: Context,
        title: String,
        description: String,
        category: String
    ) {
        val shareText = buildString {
            appendLine("🏆 BADGE UNLOCKED ON MM FITNESS 🏆")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🎖️ Achievement: $title")
            appendLine("🏷️ Category: $category")
            appendLine("📜 Feat: $description")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Crushing personal fitness milestones every day with MM Fitness!")
            appendLine("#MMFitness #AchievementUnlocked #FitnessMilestone #LevelUp")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Unlocked the '$title' Badge on MM Fitness!")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Achievement Badge")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
