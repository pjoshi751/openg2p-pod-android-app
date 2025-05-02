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
import androidx.hilt.navigation.compose.hiltViewModel
import com.openg2p.pod.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val currentServerUrl by viewModel.serverUrl.collectAsState()
    val keycloakUrl by viewModel.keycloakUrl.collectAsState()
    var serverUrlTextFieldValue by remember { mutableStateOf(currentServerUrl) }
    var keycloakUrlTextFieldValue by remember { mutableStateOf(keycloakUrl) }
    val context = LocalContext.current

    // Update text field if the underlying saved value changes
    LaunchedEffect(currentServerUrl) {
        if (serverUrlTextFieldValue != currentServerUrl) {
            serverUrlTextFieldValue = currentServerUrl
        }
    }

    LaunchedEffect(keycloakUrl) {
        if (keycloakUrlTextFieldValue != keycloakUrl) {
            keycloakUrlTextFieldValue = keycloakUrl
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
                value = serverUrlTextFieldValue,
                onValueChange = { serverUrlTextFieldValue = it },
                label = { Text("Server URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = keycloakUrlTextFieldValue,
                onValueChange = { keycloakUrlTextFieldValue = it },
                label = { Text("Keycloak Token URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(onClick = {
                viewModel.updateServerUrl(serverUrlTextFieldValue)
                viewModel.saveKeycloakUrl(keycloakUrlTextFieldValue)
                // Optionally show a toast or snackbar
                android.widget.Toast.makeText(context, "Settings saved", android.widget.Toast.LENGTH_SHORT).show()
                onNavigateBack() // Navigate back after saving
            }) {
                Text("Save Settings")
            }
        }
    }
}
