package com.prog7313.sandbox

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.prog7313.sandbox.auth.BiometricAuthManager
import com.prog7313.sandbox.auth.GoogleAuthClient
import com.prog7313.sandbox.data.BiometricPreferencesRepository
import com.prog7313.sandbox.navigation.AppNavGraph
import com.prog7313.sandbox.notifications.NotificationHelper
import com.prog7313.sandbox.theme.SandBoxTheme
import com.prog7313.sandbox.ui.AuthScreen
import com.prog7313.sandbox.ui.BiometricLockScreen
import com.prog7313.sandbox.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createChannel(this)
        enableEdgeToEdge()

        setContent {
            val settingsVm: SettingsViewModel = viewModel()
            val darkMode by settingsVm.darkMode.collectAsState()

            val auth = remember { FirebaseAuth.getInstance() }
            val googleAuthClient = remember {
                GoogleAuthClient(applicationContext)
            }
            val biometricRepository = remember {
                BiometricPreferencesRepository(applicationContext)
            }
            val biometricEnabled by biometricRepository.enabled.collectAsState(
                initial = null
            )

            val scope = rememberCoroutineScope()

            var currentUser by remember {
                mutableStateOf<FirebaseUser?>(auth.currentUser)
            }
            var sessionUnlocked by rememberSaveable {
                mutableStateOf(false)
            }

            var biometricError by remember {
                mutableStateOf<String?>(null)
            }

            DisposableEffect(auth) {
                val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    currentUser = firebaseAuth.currentUser

                    if (firebaseAuth.currentUser == null) {
                        sessionUnlocked = false
                    }
                }

                auth.addAuthStateListener(listener)

                onDispose {
                    auth.removeAuthStateListener(listener)
                }
            }

            SandBoxTheme(darkTheme = darkMode) {
                when {
                    biometricEnabled == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    currentUser == null -> {
                        AuthScreen(
                            activityContext = this,
                            googleAuthClient = googleAuthClient,
                            onAuthenticated = {
                                sessionUnlocked = true
                            }
                        )
                    }

                    biometricEnabled == true && !sessionUnlocked -> {
                        BiometricLockScreen(
                            error = biometricError,
                            onUnlock = {
                                biometricError = null

                                BiometricAuthManager.authenticate(
                                    activity = this,
                                    title = "Unlock Sandbox",
                                    subtitle = "Use biometrics or your device screen lock",
                                    onSuccess = {
                                        sessionUnlocked = true
                                    },
                                    onError = { message ->
                                        biometricError = message
                                    }
                                )
                            },
                            onLogout = {
                                scope.launch {
                                    auth.signOut()
                                    googleAuthClient.clearCredentialState()
                                    sessionUnlocked = false
                                }
                            }
                        )
                    }

                    else -> {
                        AppNavGraph(
                            settingsVm = settingsVm,
                            activity = this,
                            biometricRepository = biometricRepository,
                            onExit = { finish() },
                            onLogout = {
                                scope.launch {
                                    auth.signOut()
                                    googleAuthClient.clearCredentialState()
                                    sessionUnlocked = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}