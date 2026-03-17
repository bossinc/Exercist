package com.example.exercist.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val session by viewModel.selectedSession.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()) }

    LaunchedEffect(sessionId) { viewModel.loadSession(sessionId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.name ?: "Session") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        session?.let { s ->
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    s.date?.let { Text(dateFormat.format(it), style = MaterialTheme.typography.bodySmall) }
                    Text("Duration: ${s.durationMinutes} minutes")
                }
                items(s.exercises) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(entry.exerciseName, style = MaterialTheme.typography.titleSmall)
                            entry.sets.forEach { set ->
                                Text("Set ${set.setNumber}: ${set.weight}kg × ${set.reps} reps")
                            }
                        }
                    }
                }
            }
        } ?: Box(Modifier.padding(padding).fillMaxSize()) { Text("Loading...") }
    }
}
