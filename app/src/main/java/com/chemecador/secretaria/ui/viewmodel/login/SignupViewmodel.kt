package com.chemecador.secretaria.ui.viewmodel.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.services.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignupViewmodel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var _signupError = MutableStateFlow<String?>(null)
    val signupError: StateFlow<String?> = _signupError

    fun signup(user: String, password: String, onSignupSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = withContext(Dispatchers.IO) {
                authService.signup(user, password)
            }

            if (result.isSuccess) {
                onSignupSuccess()
                _signupError.value = null
            } else {
                _signupError.value = result.exceptionOrNull()?.message
                delay(100)
                _signupError.value = null
            }
            _isLoading.value = false
        }
    }
}
