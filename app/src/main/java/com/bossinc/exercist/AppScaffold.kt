package com.bossinc.exercist

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bossinc.exercist.navigation.BottomNavBar
import com.bossinc.exercist.navigation.NavGraph
import com.bossinc.exercist.navigation.Routes

private val bottomNavRoutes = setOf(
    Routes.Exercises.route,
    Routes.Workout.route,
    Routes.History.route
)

@Composable
fun AppScaffold(viewModel: AppViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            isAuthenticated = isAuthenticated,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
