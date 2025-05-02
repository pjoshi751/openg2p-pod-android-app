package com.openg2p.pod.network.data

import com.google.gson.annotations.SerializedName

// Successful response from Keycloak token endpoint
data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("expires_in")
    val expiresIn: Int,

    @SerializedName("refresh_expires_in")
    val refreshExpiresIn: Int,

    @SerializedName("refresh_token")
    val refreshToken: String? = null, // May not always be present depending on flow

    @SerializedName("token_type")
    val tokenType: String,

    @SerializedName("not-before-policy")
    val notBeforePolicy: Int? = null,

    @SerializedName("session_state")
    val sessionState: String? = null,

    @SerializedName("scope")
    val scope: String? = null
)

// Error response from Keycloak token endpoint
data class KeycloakError(
    @SerializedName("error")
    val error: String,

    @SerializedName("error_description")
    val errorDescription: String
)
