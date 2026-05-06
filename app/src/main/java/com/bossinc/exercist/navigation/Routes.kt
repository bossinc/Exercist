package com.bossinc.exercist.navigation

sealed class Routes(val route: String) {
    object Auth : Routes("auth")
    object Exercises : Routes("exercises")
    object ExerciseDetail : Routes("exercise_detail/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise_detail/$exerciseId"
    }
    object CreateExercise : Routes("create_exercise")
    object Workout : Routes("workout")
    object History : Routes("history")
    object SessionDetail : Routes("session_detail/{sessionId}") {
        fun createRoute(sessionId: String) = "session_detail/$sessionId"
    }
    object Profile : Routes("profile")
    object Settings : Routes("settings")
}
