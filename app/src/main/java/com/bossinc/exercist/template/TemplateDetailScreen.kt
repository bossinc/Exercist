package com.bossinc.exercist.template

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    templateId: String,
    onBack: () -> Unit,
    onStartWorkout: () -> Unit,
    viewModel: TemplateViewModel = hiltViewModel()
) {
    val template by viewModel.selectedTemplate.collectAsState()

    LaunchedEffect(templateId) { viewModel.loadTemplate(templateId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(template?.name ?: "Template") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { IconButton(onClick = onStartWorkout) { Icon(Icons.Default.PlayArrow, contentDescription = "Start Workout") } }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        template?.let { t ->
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (t.description.isNotBlank()) {
                    item { Text(t.description, style = MaterialTheme.typography.bodyMedium) }
                }
                items(t.exercises.sortedBy { it.order }) { exercise ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(exercise.exerciseName, style = MaterialTheme.typography.titleSmall)
                            Text("${exercise.targetSets} sets × ${exercise.targetReps} reps", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        } ?: Box(Modifier.padding(padding).fillMaxSize()) { Text("Loading...") }
    }
}
