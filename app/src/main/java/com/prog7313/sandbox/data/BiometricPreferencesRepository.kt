package com.prog7313.sandbox.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.biometricDataStore by preferencesDataStore(
    name = "biometric_preferences"
)

class BiometricPreferencesRepository(
    private val context: Context
) {
    private companion object {
        val ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    val enabled: Flow<Boolean> = context.biometricDataStore.data.map { preferences ->
        preferences[ENABLED] ?: false
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.biometricDataStore.edit { preferences ->
            preferences[ENABLED] = enabled
        }
    }
}