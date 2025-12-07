package com.example.familyhealth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.familyhealth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false
)

class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState(loggedIn = repo.isLoggedIn()))
    val ui: StateFlow<AuthUiState> = _ui

    fun login(email: String, password: String) {
        _ui.value = _ui.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val res = repo.login(email.trim(), password)
            _ui.value = if (res.isSuccess) {
                _ui.value.copy(loading = false, error = null, loggedIn = true)
            } else {
                _ui.value.copy(
                    loading = false,
                    error = res.exceptionOrNull()?.message ?: "Error"
                )
            }
        }
    }

    fun register(email: String, password: String) {
        _ui.value = _ui.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val res = repo.register(email.trim(), password)
            _ui.value = if (res.isSuccess) {
                _ui.value.copy(loading = false, error = null, loggedIn = true)
            } else {
                _ui.value.copy(
                    loading = false,
                    error = res.exceptionOrNull()?.message ?: "Error"
                )
            }
        }
    }

    fun logout() {
        repo.logout()
        _ui.value = AuthUiState(loggedIn = false)
    }
}
