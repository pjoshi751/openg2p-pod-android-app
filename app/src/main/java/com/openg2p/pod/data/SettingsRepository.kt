package com.openg2p.pod.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Define the DataStore instance at the top level
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    // Define default values
    companion object {
        const val DEFAULT_SERVER_URL = "http://10.0.2.2:8000" // Default for emulator
        const val DEFAULT_KEYCLOAK_URL = "https://keycloak.openg2p.org/realms/Agents/protocol/openid-connect/token"
    }

    // Define the keys for the server URL and Keycloak URL preferences
    private object PreferencesKeys {
        val SERVER_URL = stringPreferencesKey("server_url")
        val KEYCLOAK_URL = stringPreferencesKey("keycloak_url")
    }

    // Flow to observe the server URL preference
    val serverUrlFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException if it can't read the data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Get the string value associated with the key, defaulting if not set
            preferences[PreferencesKeys.SERVER_URL] ?: DEFAULT_SERVER_URL
        }

    // Flow to expose the Keycloak URL setting
    val keycloakUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.KEYCLOAK_URL] ?: DEFAULT_KEYCLOAK_URL
    }

    // Function to save the server URL
    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_URL] = url
        }
    }

    // Function to save the Keycloak URL
    suspend fun saveKeycloakUrl(url: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.KEYCLOAK_URL] = url
        }
    }
}
