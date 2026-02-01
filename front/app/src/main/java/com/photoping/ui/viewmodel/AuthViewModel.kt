package com.photoping.ui.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.photoping.PhotoPingApplication
import com.photoping.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: AuthRepository = (app as PhotoPingApplication).appContainer.authRepository

    private val _loginState = MutableStateFlow(AuthUiState())
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(AuthUiState())
    val registerState: StateFlow<AuthUiState> = _registerState.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loginState.value = AuthUiState(loading = true)
            runCatching {
                repo.login(email = email.trim(), password = password)
            }.onSuccess {
                _loginState.value = AuthUiState(loading = false)
                onSuccess()
            }.onFailure { e ->
                _loginState.value = AuthUiState(loading = false, error = e.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _registerState.value = AuthUiState(loading = true)
            runCatching {
                val cleanEmail = email.trim()
                if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                    error("Invalid email format")
                }

                val ok = repo.verifyEmail(cleanEmail)
                if (!ok) error("Email address did not pass verification")
                repo.registerAndLogin(email = cleanEmail, password = password)
            }.onSuccess {
                _registerState.value = AuthUiState(loading = false)
                onSuccess()
            }.onFailure { e ->
                _registerState.value = AuthUiState(loading = false, error = e.message ?: "Registration failed")
            }
        }
    }
}
