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

    // Define the key for the server URL preference
    private object PreferencesKeys {
        val SERVER_URL = stringPreferencesKey("server_url")
    }

    // Default URL if none is set (emulator default)
    private val defaultServerUrl = "http://10.0.2.2:8000"

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
            preferences[PreferencesKeys.SERVER_URL] ?: defaultServerUrl
        }

    // Function to save the server URL
    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_URL] = url
        }
    }
}
