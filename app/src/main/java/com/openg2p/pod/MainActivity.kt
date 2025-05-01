package com.openg2p.pod

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Required for viewModels delegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.openg2p.pod.ui.PodFormScreen
import com.openg2p.pod.ui.SettingsScreen // Import SettingsScreen
import com.openg2p.pod.ui.theme.OpenG2PPodAppTheme // Import the theme
import com.openg2p.pod.viewmodel.PodViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {

    // Use the by viewModels() delegate for ViewModel creation
    private val podViewModel: PodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            // Apply the theme from the separate theme file
            OpenG2PPodAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Setup Navigation
                    NavHost(navController = navController, startDestination = "podForm") {
                        composable("podForm") {
                            PodFormScreen(
                                viewModel = podViewModel,
                                onNavigateToSettings = { navController.navigate("settings") } // Add navigation callback
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                // SettingsViewModel is created internally by viewModel()
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
