package com.bossinc.exercist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bossinc.exercist.auth.AuthScreen
import com.bossinc.exercist.exercise.CreateExerciseScreen
import com.bossinc.exercist.exercise.ExerciseDetailScreen
import com.bossinc.exercist.exercise.ExercisesScreen
import com.bossinc.exercist.history.HistoryScreen
import com.bossinc.exercist.history.SessionDetailScreen
import com.bossinc.exercist.workout.WorkoutLogScreen

@Composable
fun NavGraph(navController: NavHostController, isAuthenticated: Boolean, modifier: Modifier = Modifier) {
    val startDestination = if (isAuthenticated) Routes.Workout.route else Routes.Auth.route

    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            navController.navigate(Routes.Auth.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Routes.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Routes.Workout.route) {
                        popUpTo(Routes.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Exercises.route) {
            ExercisesScreen(
                onExerciseClick = { id -> navController.navigate(Routes.ExerciseDetail.createRoute(id)) },
                onCreateExercise = { navController.navigate(Routes.CreateExercise.route) }
            )
        }
        composable(
            Routes.ExerciseDetail.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            ExerciseDetailScreen(
                exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.CreateExercise.route) {
            CreateExerciseScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Workout.route) {
            WorkoutLogScreen(navController = navController)
        }
        composable(Routes.History.route) {
            HistoryScreen(
                onSessionClick = { id -> navController.navigate(Routes.SessionDetail.createRoute(id)) }
            )
        }
        composable(
            Routes.SessionDetail.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            SessionDetailScreen(
                sessionId = backStackEntry.arguments?.getString("sessionId") ?: "",
                onBack = { navController.popBackStack() },
                onContinueWorkout = {
                    navController.navigate(Routes.Workout.route) {
                        popUpTo(Routes.Workout.route) { inclusive = true }
                    }
                },
                onCopyWorkout = {
                    navController.navigate(Routes.Workout.route) {
                        popUpTo(Routes.Workout.route) { inclusive = true }
                    }
                },
                onNavigateToHistory = {
                    navController.navigate(Routes.History.route) {
                        popUpTo(Routes.History.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
