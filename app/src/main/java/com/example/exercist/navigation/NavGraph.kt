package com.example.exercist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.exercist.auth.AuthScreen
import com.example.exercist.exercise.CreateExerciseScreen
import com.example.exercist.exercise.ExerciseDetailScreen
import com.example.exercist.exercise.ExercisesScreen
import com.example.exercist.history.HistoryScreen
import com.example.exercist.history.SessionDetailScreen
import com.example.exercist.profile.ProfileScreen
import com.example.exercist.profile.SettingsScreen
import com.example.exercist.progress.PRsScreen
import com.example.exercist.progress.ProgressScreen
import com.example.exercist.template.CreateTemplateScreen
import com.example.exercist.template.TemplateDetailScreen
import com.example.exercist.template.TemplatesScreen
import com.example.exercist.workout.WorkoutLogScreen
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Text

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Routes.Home.route
    } else {
        Routes.Auth.route
    }

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Routes.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Home.route) {
            Text("Home — start a workout or pick a template")
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
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Progress.route) {
            ProgressScreen()
        }
        composable(Routes.PRs.route) {
            PRsScreen()
        }
        composable(Routes.Profile.route) {
            ProfileScreen(onSettings = { navController.navigate(Routes.Settings.route) })
        }
        composable(Routes.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
