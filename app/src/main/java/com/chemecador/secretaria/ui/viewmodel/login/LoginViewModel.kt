package com.chemecador.secretaria.ui.viewmodel.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.network.services.AuthService
import com.chemecador.secretaria.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val userRepository: UserRepository
) : ViewModel() {

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError


    fun login(user: String, password: String, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = withContext(Dispatchers.IO) {
                authService.login(user, password)
            }

            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    userRepository.saveUserDetails(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email
                    )

                    onLoginSuccess()
                    _loginError.emit(null)
                }
            } else {
                _loginError.emit(result.exceptionOrNull()?.message)
                Timber.e(result.exceptionOrNull())
                _loginError.emit(null)
            }

            _isLoading.value = false
        }
    }


    fun signInWithGoogle(idToken: String, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = withContext(Dispatchers.IO) {
                authService.signInWithGoogle(idToken)
            }

            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    userRepository.saveUserDetails(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email,
                    )

                    onLoginSuccess()
                    _loginError.emit(null)
                }
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                _loginError.emit(errorMessage)
                Timber.e(errorMessage)
                _loginError.emit(null)
            }

            _isLoading.value = false
        }
    }

}