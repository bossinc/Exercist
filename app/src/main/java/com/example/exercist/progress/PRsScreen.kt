package com.example.exercist.progress

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
fun PRsScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val prs by viewModel.prs.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(topBar = { TopAppBar(title = { Text("Personal Records") }) }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (prs.isEmpty()) {
                item { Text("No personal records yet. Keep training!") }
            }
            items(prs) { pr ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(pr.exerciseName, style = MaterialTheme.typography.titleSmall)
                        Text("${pr.weight}${pr.unit} × ${pr.reps} reps")
                        pr.achievedAt?.let { Text(dateFormat.format(it), style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}
