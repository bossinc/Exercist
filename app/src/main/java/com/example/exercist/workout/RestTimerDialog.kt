package com.example.exercist.workout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RestTimerDialog(
    secondsRemaining: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rest Timer") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                val minutes = secondsRemaining / 60
                val seconds = secondsRemaining % 60
                Text(
                    "%d:%02d".format(minutes, seconds),
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { secondsRemaining / 90f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Skip") }
        }
    )
}
