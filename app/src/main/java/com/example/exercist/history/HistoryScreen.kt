package com.example.exercist.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onSessionClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(topBar = { TopAppBar(title = { Text("History") }) }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                WorkoutCalendar(sessions = sessions, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
            }
            items(sessions) { session ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onSessionClick(session.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(session.name, style = MaterialTheme.typography.titleMedium)
                        session.date?.let { Text(dateFormat.format(it), style = MaterialTheme.typography.bodySmall) }
                        Text("${session.exercises.size} exercises · ${session.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
