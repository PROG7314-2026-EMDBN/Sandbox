package com.prog7313.sandbox.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.prog7313.sandbox.auth.BiometricAuthManager
import com.prog7313.sandbox.data.BiometricPreferencesRepository
import com.prog7313.sandbox.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsVm: SettingsViewModel,
    activity: FragmentActivity,
    biometricRepository: BiometricPreferencesRepository
) {
    val darkMode by settingsVm.darkMode.collectAsState()
    val biometricEnabled by biometricRepository.enabled.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Appearance", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Card {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dark mode", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Toggle between light and dark theme.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Switch(
                    checked = darkMode,
                    onCheckedChange = settingsVm::setDarkMode
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Security", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Card {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Biometric app lock",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Require biometrics or the device screen lock when Sandbox starts.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Switch(
                    checked = biometricEnabled,
                    onCheckedChange = { requestedValue ->
                        if (!BiometricAuthManager.isAvailable(activity)) {
                            return@Switch
                        }

                        BiometricAuthManager.authenticate(
                            activity = activity,
                            title = if (requestedValue) {
                                "Enable biometric lock"
                            } else {
                                "Disable biometric lock"
                            },
                            subtitle = "Confirm your identity",
                            onSuccess = {
                                scope.launch {
                                    biometricRepository.setEnabled(requestedValue)
                                }
                            },
                            onError = { }
                        )
                    }
                )
            }
        }
    }
}