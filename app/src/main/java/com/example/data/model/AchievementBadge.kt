package com.example.data.model

enum class BadgeCategory(val displayName: String) {
    ALL("All"),
    WORKOUT("Workout"),
    STEPS("Steps"),
    DISTANCE("Distance"),
    HYDRATION("Hydration"),
    CALORIES("Calories")
}

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val category: BadgeCategory,
    val iconKey: String, // e.g. "early_bird", "marathoner", "night_owl", "step_master", "hydro_master", "century_sprint", "calorie_crusher", "iron_will"
    val accentColorHex: String,
    val isUnlocked: Boolean = false,
    val unlockedTimestamp: Long? = null,
    val currentValue: Int = 0,
    val targetValue: Int = 1,
    val unit: String = ""
) {
    val progress: Float
        get() = if (isUnlocked) 1f else if (targetValue > 0) (currentValue.toFloat() / targetValue).coerceIn(0f, 1f) else 0f
}
