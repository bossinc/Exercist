package com.bossinc.exercist.exercise

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.bossinc.exercist.data.model.Exercise
import com.bossinc.exercist.data.model.MuscleGroups
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    onExerciseClick: (String) -> Unit,
    onCreateExercise: () -> Unit,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGroup by viewModel.selectedMuscleGroup.collectAsState()
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (!json.isNullOrBlank()) {
                viewModel.importExercises(parseImportJson(json))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercises") },
                actions = {
                    IconButton(onClick = onCreateExercise) {
                        Icon(Icons.Default.Add, contentDescription = "Add Exercise")
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export data") },
                            onClick = {
                                menuExpanded = false
                                val json = buildExportJson(allExercises)
                                val file = File(context.cacheDir, "exercist_exercises_export.json")
                                file.writeText(json)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Export exercises"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import data") },
                            onClick = {
                                menuExpanded = false
                                importLauncher.launch("application/json")
                            }
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = selectedGroup == null, onClick = { viewModel.setMuscleGroup(null) }, label = { Text("All") })
                }
                items(MuscleGroups.all) { group ->
                    FilterChip(selected = selectedGroup == group, onClick = { viewModel.setMuscleGroup(if (selectedGroup == group) null else group) }, label = { Text(group) })
                }
            }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(exercises) { exercise ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onExerciseClick(exercise.id) }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                            Text("${exercise.muscleGroup} · ${exercise.equipment}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private fun buildExportJson(exercises: List<Exercise>): String {
    val array = JSONArray()
    exercises.forEach { exercise ->
        val obj = JSONObject()
        obj.put("id", exercise.id)
        obj.put("name", exercise.name)
        obj.put("muscleGroup", exercise.muscleGroup)
        obj.put("equipment", exercise.equipment)
        obj.put("description", exercise.description)
        array.put(obj)
    }
    return array.toString(2)
}

private fun parseImportJson(json: String): List<Exercise> {
    val array = JSONArray(json)
    return List(array.length()) { i ->
        val obj = array.getJSONObject(i)
        Exercise(
            id = obj.optString("id"),
            name = obj.optString("name"),
            muscleGroup = obj.optString("muscleGroup"),
            equipment = obj.optString("equipment"),
            description = obj.optString("description")
        )
    }
}
