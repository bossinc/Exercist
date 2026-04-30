package com.bossinc.exercist.history

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.bossinc.exercist.data.model.WorkoutSession
import com.bossinc.exercist.exercise.ExerciseViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onSessionClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
    exerciseViewModel: ExerciseViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val exercises by exerciseViewModel.exercises.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault()) }
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                actions = {
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
                                val json = buildExportJson(sessions)
                                val file = File(context.cacheDir, "exercist_export.json")
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
                                context.startActivity(Intent.createChooser(intent, "Export workout data"))
                            }
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sessions) { session ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onSessionClick(session.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        val displayDate = session.startedAt ?: session.date
                        Text(displayDate?.let { dateFormat.format(it) } ?: "Workout", style = MaterialTheme.typography.titleMedium)
                        val muscleGroups = session.exercises
                            .map { entry -> exercises.find { it.id == entry.exerciseId }?.muscleGroup ?: entry.muscleGroup }
                            .filter { it.isNotBlank() }
                            .groupBy { it }
                            .map { (group, entries) -> "$group (${entries.size})" }
                            .joinToString(" · ")
                        Text(
                            if (muscleGroups.isNotBlank()) muscleGroups else "${session.exercises.size} exercises",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text("${session.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private fun buildExportJson(sessions: List<WorkoutSession>): String {
    val array = JSONArray()
    sessions.forEach { session ->
        val obj = JSONObject()
        obj.put("id", session.id)
        obj.put("name", session.name)
        obj.put("durationMinutes", session.durationMinutes)
        obj.put("notes", session.notes)
        obj.put("date", session.date?.time ?: JSONObject.NULL)
        obj.put("startedAt", session.startedAt?.time ?: JSONObject.NULL)
        obj.put("finishedAt", session.finishedAt?.time ?: JSONObject.NULL)
        val exercisesArray = JSONArray()
        session.exercises.forEach { entry ->
            val entryObj = JSONObject()
            entryObj.put("exerciseId", entry.exerciseId)
            entryObj.put("exerciseName", entry.exerciseName)
            entryObj.put("muscleGroup", entry.muscleGroup)
            entryObj.put("notes", entry.notes)
            val setsArray = JSONArray()
            entry.sets.forEach { set ->
                val setObj = JSONObject()
                setObj.put("setNumber", set.setNumber)
                setObj.put("reps", set.reps)
                setObj.put("weight", set.weight)
                setObj.put("unit", set.unit)
                setsArray.put(setObj)
            }
            entryObj.put("sets", setsArray)
            exercisesArray.put(entryObj)
        }
        obj.put("exercises", exercisesArray)
        array.put(obj)
    }
    return array.toString(2)
}
