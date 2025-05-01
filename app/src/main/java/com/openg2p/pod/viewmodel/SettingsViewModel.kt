package com.openg2p.pod.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openg2p.pod.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // Get the repository instance (Consider Dependency Injection for larger apps)
    private val settingsRepository = SettingsRepository(application.applicationContext)

    // Expose the server URL flow as StateFlow for the UI
    val serverUrl: StateFlow<String> = settingsRepository.serverUrlFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep collecting for 5s after last subscriber
            initialValue = "" // Initial value while loading
        )

    // Function to update the server URL
    fun updateServerUrl(newUrl: String) {
        viewModelScope.launch {
            settingsRepository.saveServerUrl(newUrl)
        }
    }
}
