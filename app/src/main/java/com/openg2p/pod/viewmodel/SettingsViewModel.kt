package com.openg2p.pod.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openg2p.pod.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Expose the server URL flow as StateFlow for the UI
    val serverUrl: StateFlow<String> = settingsRepository.serverUrlFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep collecting for 5s after last subscriber
            initialValue = "" // Initial value while loading
        )

    // Expose the Keycloak URL flow as StateFlow for the UI
    val keycloakUrl: StateFlow<String> = settingsRepository.keycloakUrlFlow
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

    // Function to save the Keycloak URL
    fun saveKeycloakUrl(url: String) = viewModelScope.launch {
        settingsRepository.saveKeycloakUrl(url)
    }
}
