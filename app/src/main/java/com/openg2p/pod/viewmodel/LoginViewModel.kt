package com.openg2p.pod.viewmodel

import androidx.compose.runtime.State
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
    val loginSuccess: Boolean = false,
    val agentId: String = "",
    val password: String = ""
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(LoginUiState()) // Private MutableState
    val uiState: State<LoginUiState> = _uiState // Public immutable State

    // Update Agent ID in the state
    fun onAgentIdChanged(newAgentId: String) {
        _uiState.value = _uiState.value.copy(agentId = newAgentId)
    }

    // Update Password in the state
    fun onPasswordChanged(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword)
    }

    fun login() {
        // Reset error message immediately on attempt
        _uiState.value = _uiState.value.copy(errorMessage = null)

        if (_uiState.value.agentId.isBlank() || _uiState.value.password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Agent ID and Password cannot be empty.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = authRepository.login(_uiState.value.agentId, _uiState.value.password)) {
                is AuthResult.Success -> {
                    // Role check is now inside AuthRepository
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true, errorMessage = null)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun resetLoginStatus() {
        _uiState.value = _uiState.value.copy(loginSuccess = false)
    }
}
