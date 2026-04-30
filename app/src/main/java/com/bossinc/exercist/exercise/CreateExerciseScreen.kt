package com.bossinc.exercist.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bossinc.exercist.data.model.Exercise
import com.bossinc.exercist.data.model.MuscleGroups

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExerciseScreen(
    onBack: () -> Unit,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf(MuscleGroups.all.first()) }
    var equipment by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Exercise") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = muscleGroup,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Muscle Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    MuscleGroups.all.forEach { group ->
                        DropdownMenuItem(text = { Text(group) }, onClick = { muscleGroup = group; expanded = false })
                    }
                }
            }

            OutlinedTextField(value = equipment, onValueChange = { equipment = it }, label = { Text("Equipment") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

            Button(
                onClick = {
                    viewModel.createExercise(Exercise(name = name, muscleGroup = muscleGroup, equipment = equipment, description = description))
                    onBack()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Create") }
        }
    }
}
