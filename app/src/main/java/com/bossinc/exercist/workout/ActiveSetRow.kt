package com.bossinc.exercist.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ActiveSetRow(
    exerciseId: String,
    setNumber: Int,
    initialReps: Int = 0,
    initialWeight: Int = 0,
    onValuesChange: (reps: Int, weight: Int) -> Unit
) {
    var reps by remember(exerciseId, setNumber) { mutableStateOf(TextFieldValue(if (initialReps > 0) initialReps.toString() else "")) }
    var weight by remember(exerciseId, setNumber) { mutableStateOf(TextFieldValue(if (initialWeight > 0) initialWeight.toString() else "")) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Set $setNumber", modifier = Modifier.width(48.dp))
        OutlinedTextField(
            value = weight,
            onValueChange = {
                weight = it
                onValuesChange(reps.text.toIntOrNull() ?: 0, it.text.toIntOrNull() ?: 0)
            },
            label = { Text("lbs") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { if (it.isFocused) scope.launch { weight = weight.copy(selection = TextRange(0, weight.text.length)) } },
            singleLine = true
        )
        OutlinedTextField(
            value = reps,
            onValueChange = {
                reps = it
                onValuesChange(it.text.toIntOrNull() ?: 0, weight.text.toIntOrNull() ?: 0)
            },
            label = { Text("reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { if (it.isFocused) scope.launch { reps = reps.copy(selection = TextRange(0, reps.text.length)) } },
            singleLine = true
        )
    }
}
