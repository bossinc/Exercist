package com.bossinc.exercist.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bossinc.exercist.BuildConfig
import com.bossinc.exercist.R

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val webClientId = context.getString(R.string.default_web_client_id)

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthSuccess()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Exercist", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(48.dp))

        uiState.error?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = { viewModel.signInWithGoogle(context, webClientId) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.size(16.dp))
            } else {
                Text("Sign in with Google")
            }
        }

        if (BuildConfig.DEBUG) {
            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = { viewModel.skipAuth() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip (debug only)")
            }
        }
    }
}
