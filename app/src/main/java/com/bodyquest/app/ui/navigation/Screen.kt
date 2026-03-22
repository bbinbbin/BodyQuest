package com.bodyquest.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Quest : Screen("quest")
    object QuestTree : Screen("quest_tree/{category}") {
        fun createRoute(category: String) = "quest_tree/$category"
    }
    object QuestDetail : Screen("quest_detail/{questId}") {
        fun createRoute(questId: String) = "quest_detail/$questId"
    }
    object Workout : Screen("workout/{questId}") {
        fun createRoute(questId: String) = "workout/$questId"
    }
    object WorkoutComplete : Screen("workout_complete/{workoutId}") {
        fun createRoute(workoutId: Long) = "workout_complete/$workoutId"
    }
    object Pvp : Screen("pvp")
    object Avatar : Screen("avatar")
    object Profile : Screen("profile")
}
