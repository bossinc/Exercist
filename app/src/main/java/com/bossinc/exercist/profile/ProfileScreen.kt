package com.bossinc.exercist.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = { IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, null) } }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(user?.displayName?.ifBlank { user?.email } ?: "User", style = MaterialTheme.typography.headlineMedium)
            user?.email?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Goals", style = MaterialTheme.typography.titleMedium)
                    Text("Weekly workouts: ${user?.goals?.weeklyWorkouts ?: 3}")
                    user?.goals?.targetBodyWeight?.let { Text("Target weight: ${it}lbs") }
                }
            }

            Button(onClick = viewModel::signOut, modifier = Modifier.fillMaxWidth()) {
                Text("Sign Out")
            }
        }
    }
}
