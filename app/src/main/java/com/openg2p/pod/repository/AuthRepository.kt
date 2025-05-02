package com.openg2p.pod.repository

import android.util.Log
import com.auth0.android.jwt.JWT
import com.google.gson.Gson
import com.openg2p.pod.data.SettingsRepository
import com.openg2p.pod.network.KeycloakApiService
import com.openg2p.pod.network.data.KeycloakError
import com.openg2p.pod.network.data.TokenResponse
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

// Simple Result wrapper for repository calls
sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : AuthResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val keycloakApiService: KeycloakApiService
) {
    private val _tag = "AuthRepository"
    private val CLIENT_ID = "openg2p_pod_app" // Move to config later if needed
    private val REQUIRED_ROLE = "DELIVERY_AGENT"

    suspend fun login(username: String, password: String): AuthResult<TokenResponse> {
        return try {
            val keycloakUrl = settingsRepository.keycloakUrlFlow.first() // Get the URL from settings
            Log.d(_tag, "Attempting login to: $keycloakUrl")

            val response = keycloakApiService.getToken(
                url = keycloakUrl,
                clientId = CLIENT_ID,
                username = username,
                password = password
            )

            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                Log.i(_tag, "Login successful, token received.")

                // --- JWT Role Check ---
                try {
                    val jwt = JWT(tokenResponse.accessToken)
                    val realmAccess = jwt.getClaim("realm_access").asObject(Map::class.java)
                    val roles = realmAccess?.get("roles") as? List<*>
                    if (roles != null && roles.contains(REQUIRED_ROLE)) {
                        Log.i(_tag, "Required role '$REQUIRED_ROLE' found in token.")
                        AuthResult.Success(tokenResponse)
                    } else {
                        Log.w(_tag, "Required role '$REQUIRED_ROLE' not found in token. Roles: $roles")
                        AuthResult.Error("User does not have the required role: $REQUIRED_ROLE")
                    }
                } catch (e: Exception) {
                    Log.e(_tag, "Error decoding JWT or checking roles", e)
                    AuthResult.Error("Failed to verify user roles.", e)
                }
                // --- End JWT Role Check ---

            } else {
                val errorBody = response.errorBody()?.string()
                var errorMessage = "Login failed with code: ${response.code()}"
                if (errorBody != null) {
                    try {
                        val keycloakError = Gson().fromJson(errorBody, KeycloakError::class.java)
                        errorMessage = keycloakError.errorDescription
                        Log.w(_tag, "Keycloak error: ${keycloakError.error} - $errorMessage")
                    } catch (e: Exception) {
                        Log.e(_tag, "Failed to parse error body: $errorBody", e)
                        errorMessage += " (Could not parse error details)"
                    }
                }
                Log.w(_tag, errorMessage)
                AuthResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(_tag, "Login network call failed", e)
            AuthResult.Error("Network error: ${e.message ?: "Unknown"}", e)
        }
    }

    // TODO: Add function to store/retrieve token securely (e.g., EncryptedSharedPreferences)
    // TODO: Add function for token refresh
    // TODO: Add function for logout
}
