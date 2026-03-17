package com.example.exercist.progress

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val sessions by viewModel.sessions.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Progress") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Workout Frequency", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Total sessions: ${sessions.size}", style = MaterialTheme.typography.bodyLarge)
                    val lastMonth = sessions.count { session ->
                        session.date?.let { date ->
                            val now = System.currentTimeMillis()
                            now - date.time < 30L * 24 * 60 * 60 * 1000
                        } ?: false
                    }
                    Text("Last 30 days: $lastMonth sessions")
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Volume", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Track your workout volume over time as you log more sessions.")
                }
            }
        }
    }
}
