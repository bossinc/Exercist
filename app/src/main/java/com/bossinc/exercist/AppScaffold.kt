package com.bossinc.exercist

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bossinc.exercist.navigation.BottomNavBar
import com.bossinc.exercist.navigation.NavGraph
import com.bossinc.exercist.navigation.Routes

private val bottomNavRoutes = setOf(
    Routes.Home.route,
    Routes.Exercises.route,
    Routes.Workout.route,
    Routes.History.route,
    Routes.Progress.route
)

@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
