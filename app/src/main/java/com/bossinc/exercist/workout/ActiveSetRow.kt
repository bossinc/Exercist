package com.bossinc.exercist.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ActiveSetRow(
    setNumber: Int,
    initialReps: Int = 0,
    initialWeight: Double = 0.0,
    isCompleted: Boolean = false,
    onComplete: (reps: Int, weight: Double) -> Unit
) {
    var reps by remember { mutableStateOf(if (initialReps > 0) initialReps.toString() else "") }
    var weight by remember { mutableStateOf(if (initialWeight > 0) initialWeight.toString() else "") }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Set $setNumber", modifier = Modifier.width(48.dp))
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("kg") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it },
            label = { Text("reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Checkbox(
            checked = isCompleted,
            onCheckedChange = {
                onComplete(reps.toIntOrNull() ?: 0, weight.toDoubleOrNull() ?: 0.0)
            }
        )
    }
}
