package com.prog7313.sandbox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prog7313.sandbox.data.AuthRepository
import com.prog7313.sandbox.data.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val profileRepo = ProfileRepository()

    private val _uiState = MutableStateFlow(
        AuthUiState(isAuthenticated = repo.currentUser != null)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = message
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            showError("Email and password are required")
            return
        }

        runAuthentication {
            repo.login(email, password)
        }
    }

    fun register() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            showError("Email and password are required")
            return
        }

        runAuthentication {
            repo.register(email, password)
        }
    }

    fun loginWithGoogleToken(idToken: String) {
        runAuthentication {
            repo.loginWithGoogle(idToken)
        }
    }

    private fun runAuthentication(
        operation: suspend () -> Result<com.google.firebase.auth.FirebaseUser>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val result = operation()

            _uiState.value = if (result.isSuccess) {
                val user = result.getOrNull()

                if (user != null) {
                    profileRepo.ensureProfileExists(
                        uid = user.uid,
                        email = user.email.orEmpty()
                    )
                }

                _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                        ?: "Authentication failed"
                )
            }
        }
    }
}