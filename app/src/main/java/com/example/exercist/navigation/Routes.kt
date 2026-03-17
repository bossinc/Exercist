package com.example.exercist.navigation

sealed class Routes(val route: String) {
    object Auth : Routes("auth")
    object Home : Routes("home")
    object Exercises : Routes("exercises")
    object ExerciseDetail : Routes("exercise_detail/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise_detail/$exerciseId"
    }
    object CreateExercise : Routes("create_exercise")
    object Workout : Routes("workout")
    object Templates : Routes("templates")
    object TemplateDetail : Routes("template_detail/{templateId}") {
        fun createRoute(templateId: String) = "template_detail/$templateId"
    }
    object CreateTemplate : Routes("create_template")
    object History : Routes("history")
    object SessionDetail : Routes("session_detail/{sessionId}") {
        fun createRoute(sessionId: String) = "session_detail/$sessionId"
    }
    object Progress : Routes("progress")
    object PRs : Routes("prs")
    object Profile : Routes("profile")
    object Settings : Routes("settings")
}
