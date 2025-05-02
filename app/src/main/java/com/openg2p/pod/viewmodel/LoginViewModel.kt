package com.openg2p.pod.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openg2p.pod.repository.AuthRepository
import com.openg2p.pod.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

// Data class to hold login state
data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var agentId by mutableStateOf("")
    var password by mutableStateOf("")
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun login() {
        // Reset error message immediately on attempt
        uiState = uiState.copy(errorMessage = null)

        if (agentId.isBlank() || password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Agent ID and Password cannot be empty.")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = authRepository.login(agentId, password)) {
                is AuthResult.Success -> {
                    // Role check is now inside AuthRepository
                    uiState = uiState.copy(isLoading = false, loginSuccess = true)
                }
                is AuthResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun resetLoginStatus() {
        uiState = uiState.copy(loginSuccess = false)
    }
}
