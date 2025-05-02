package com.openg2p.pod

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Required for viewModels delegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openg2p.pod.ui.PodFormScreen
import com.openg2p.pod.ui.SettingsScreen // Import SettingsScreen
import com.openg2p.pod.ui.screens.LoginScreen // Import LoginScreen
import com.openg2p.pod.ui.theme.OpenG2PPodAppTheme // Import the theme
import com.openg2p.pod.viewmodel.PodViewModel
import dagger.hilt.android.AndroidEntryPoint

// Navigation Routes
const val LOGIN_ROUTE = "login"
const val FORM_ROUTE = "form"
const val SETTINGS_ROUTE = "settings"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Use the by viewModels() delegate for ViewModel creation
    private val podViewModel: PodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenG2PPodAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = LOGIN_ROUTE) {
        composable(LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(FORM_ROUTE) {
                        // Pop login screen off the back stack
                        popUpTo(LOGIN_ROUTE) {
                            inclusive = true
                        }
                        // Avoid multiple copies of the form screen
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(FORM_ROUTE) {
            PodFormScreen(
                onNavigateToSettings = { navController.navigate(SETTINGS_ROUTE) }
            )
        }
        composable(SETTINGS_ROUTE) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
