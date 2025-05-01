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
import com.openg2p.pod.ui.theme.OpenG2PPodAppTheme // Import the theme
import com.openg2p.pod.viewmodel.PodViewModel

class MainActivity : ComponentActivity() {

    // Use the by viewModels() delegate for ViewModel creation
    private val podViewModel: PodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply the theme from the separate theme file
            OpenG2PPodAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the ViewModel instance to the screen
                    PodFormScreen(viewModel = podViewModel)
                }
            }
        }
    }
}
