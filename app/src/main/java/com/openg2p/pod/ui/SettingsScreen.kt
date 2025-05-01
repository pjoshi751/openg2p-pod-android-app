package com.openg2p.pod.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openg2p.pod.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val currentServerUrl by settingsViewModel.serverUrl.collectAsState()
    var textFieldValue by remember { mutableStateOf(currentServerUrl) }
    val context = LocalContext.current

    // Update text field if the underlying saved value changes
    LaunchedEffect(currentServerUrl) {
        if (textFieldValue != currentServerUrl) {
            textFieldValue = currentServerUrl
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = { Text("Server URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(onClick = {
                settingsViewModel.updateServerUrl(textFieldValue)
                // Optionally show a toast or snackbar
                android.widget.Toast.makeText(context, "Server URL saved", android.widget.Toast.LENGTH_SHORT).show()
                onNavigateBack() // Navigate back after saving
            }) {
                Text("Save Settings")
            }
        }
    }
}
