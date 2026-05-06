package com.bossinc.exercist.history

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.bossinc.exercist.data.model.ExerciseEntry
import com.bossinc.exercist.data.model.ExerciseSet
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

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (!json.isNullOrBlank()) {
                viewModel.importSessions(parseImportJson(json))
            }
        }
    }

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
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sessions) { session ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onSessionClick(session.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        val displayDate = session.startedAt
                        Text(displayDate?.let { dateFormat.format(it) } ?: "Workout", style = MaterialTheme.typography.titleMedium)
                        val muscleGroups = session.exercises
                            .mapNotNull { entry -> exercises.find { it.id == entry.exerciseId }?.muscleGroup?.takeIf { it.isNotBlank() } }
                            .groupBy { it }
                            .map { (group, entries) -> "$group (${entries.size})" }
                            .joinToString(" · ")
                        Text(
                            if (muscleGroups.isNotBlank()) muscleGroups else "${session.exercises.size} exercises",
                            style = MaterialTheme.typography.bodySmall
                        )
                        val durationMinutes = if (session.startedAt != null && session.finishedAt != null)
                            ((session.finishedAt.time - session.startedAt.time) / 60000).toInt() else null
                        durationMinutes?.let { Text("$it min", style = MaterialTheme.typography.bodySmall) }
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
        obj.put("startedAt", session.startedAt?.time ?: JSONObject.NULL)
        obj.put("finishedAt", session.finishedAt?.time ?: JSONObject.NULL)
        val exercisesArray = JSONArray()
        session.exercises.forEach { entry ->
            val entryObj = JSONObject()
            entryObj.put("exerciseId", entry.exerciseId)
            entryObj.put("notes", entry.notes)
            val setsArray = JSONArray()
            entry.sets.forEach { set ->
                val setObj = JSONObject()
                setObj.put("reps", set.reps)
                setObj.put("weight", set.weight)
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

private fun parseImportJson(json: String): List<WorkoutSession> {
    val array = JSONArray(json)
    return List(array.length()) { i ->
        val obj = array.getJSONObject(i)
        val startedAt = obj.opt("startedAt").let { if (it is Number) Date(it.toLong()) else null }
        val finishedAt = obj.opt("finishedAt").let { if (it is Number) Date(it.toLong()) else null }
        val exercisesArray = obj.optJSONArray("exercises") ?: JSONArray()
        val entries = List(exercisesArray.length()) { j ->
            val entryObj = exercisesArray.getJSONObject(j)
            val setsArray = entryObj.optJSONArray("sets") ?: JSONArray()
            val sets = List(setsArray.length()) { k ->
                val setObj = setsArray.getJSONObject(k)
                ExerciseSet(reps = setObj.optInt("reps"), weight = setObj.optInt("weight"))
            }
            ExerciseEntry(
                exerciseId = entryObj.optString("exerciseId"),
                sets = sets,
                notes = entryObj.optString("notes")
            )
        }
        WorkoutSession(
            id = obj.optString("id"),
            exercises = entries,
            startedAt = startedAt,
            finishedAt = finishedAt
        )
    }
}
