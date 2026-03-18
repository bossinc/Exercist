package com.bossinc.exercist.navigation

import androidx.compose.runtime.Composable
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
import com.bossinc.exercist.profile.ProfileScreen
import com.bossinc.exercist.profile.SettingsScreen
import com.bossinc.exercist.template.CreateTemplateScreen
import com.bossinc.exercist.template.TemplateDetailScreen
import com.bossinc.exercist.template.TemplatesScreen
import com.bossinc.exercist.workout.WorkoutLogScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Routes.Workout.route
    } else {
        Routes.Auth.route
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
        composable(Routes.Templates.route) {
            TemplatesScreen(
                onTemplateClick = { id -> navController.navigate(Routes.TemplateDetail.createRoute(id)) },
                onCreateTemplate = { navController.navigate(Routes.CreateTemplate.route) }
            )
        }
        composable(
            Routes.TemplateDetail.route,
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->
            TemplateDetailScreen(
                templateId = backStackEntry.arguments?.getString("templateId") ?: "",
                onBack = { navController.popBackStack() },
                onStartWorkout = { navController.navigate(Routes.Workout.route) }
            )
        }
        composable(Routes.CreateTemplate.route) {
            CreateTemplateScreen(onBack = { navController.popBackStack() })
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
        composable(Routes.Profile.route) {
            ProfileScreen(onSettings = { navController.navigate(Routes.Settings.route) })
        }
        composable(Routes.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
