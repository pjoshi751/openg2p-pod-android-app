package com.openg2p.pod.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openg2p.pod.R
import com.openg2p.pod.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // The 'by' delegate automatically observes the State object from mutableStateOf
    val uiState by viewModel.uiState

    // Effect to trigger navigation when login is successful
    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            onLoginSuccess()
            viewModel.resetLoginStatus() // Reset status after navigation
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Proof of Delivery",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold // Make title bold
                    )
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_openg2p_logo),
                        contentDescription = stringResource(id = R.string.app_name),
                        modifier = Modifier.padding(start = 8.dp).size(40.dp)
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Agent Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.agentId, // Use agentId from uiState
                onValueChange = { newId -> viewModel.onAgentIdChanged(newId) }, // Explicit lambda
                label = { Text("Agent ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
                isError = uiState.errorMessage != null // Check error message in uiState
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.password, // Use password from uiState
                onValueChange = { newPassword -> viewModel.onPasswordChanged(newPassword) }, // Explicit lambda
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !uiState.isLoading,
                isError = uiState.errorMessage != null // Check error message in uiState
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                // Display loading indicator using isLoading from uiState
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.login() }, // Call ViewModel's login function
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.agentId.isNotBlank() && uiState.password.isNotBlank() && !uiState.isLoading // Enable based on uiState
                ) {
                    Text("Login")
                }
            }

            // Display error message from uiState
            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
