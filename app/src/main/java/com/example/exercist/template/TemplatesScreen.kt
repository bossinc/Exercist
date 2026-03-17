package com.example.exercist.template

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onTemplateClick: (String) -> Unit,
    onCreateTemplate: () -> Unit,
    viewModel: TemplateViewModel = hiltViewModel()
) {
    val templates by viewModel.templates.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Templates") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTemplate) {
                Icon(Icons.Default.Add, contentDescription = "Create Template")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(templates) { template ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onTemplateClick(template.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(template.name, style = MaterialTheme.typography.titleMedium)
                        Text("${template.exercises.size} exercises", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
