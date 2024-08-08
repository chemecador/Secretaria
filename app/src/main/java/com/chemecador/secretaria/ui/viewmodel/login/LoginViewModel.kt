package com.chemecador.secretaria.ui.viewmodel.login

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.repositories.UserRepository
import com.chemecador.secretaria.data.services.AuthService
import com.chemecador.secretaria.utils.Resource
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val userRepository: UserRepository
) : ViewModel() {

    private var verificationCode: String = ""
    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _authState = MutableLiveData<Resource<FirebaseUser>>()
    val authState: LiveData<Resource<FirebaseUser>> get() = _authState


    fun login(user: String, password: String, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = withContext(Dispatchers.IO) {
                authService.login(user, password)
            }

            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    onLoginSuccess()
                    _loginError.emit(null)
                }
            } else {
                _loginError.emit(result.exceptionOrNull()?.message)
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
                    onLoginSuccess()
                    _loginError.emit(null)
                }
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Error"
                _loginError.emit(errorMessage)
                _loginError.emit(null)
            }

            _isLoading.value = false
        }
    }

    fun loginWithPhone(
        phoneNumber: String,
        activity: Activity,
        onVerificationComplete: () -> Unit,
        onCodeSent: () -> Unit,
        onVerificationFailed: (String) -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credentials: PhoneAuthCredential) {

                    viewModelScope.launch {
                        val result = withContext(Dispatchers.IO) {
                            authService.completeRegisterWithPhoneVerification(credentials)
                        }

                        if (result.isSuccess) {
                            val firebaseUser = result.getOrNull()
                            if (firebaseUser != null) {
                                onVerificationComplete()
                                _loginError.emit(null)
                            }
                        } else {
                            _loginError.emit(result.exceptionOrNull()?.message)
                        }

                        _isLoading.value = false
                    }
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    _isLoading.value = false
                    onVerificationFailed(p0.message.orEmpty())
                }

                override fun onCodeSent(
                    verificationCode: String, p1: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@LoginViewModel.verificationCode = verificationCode
                    _isLoading.value = false
                    onCodeSent()
                }

            }

            withContext(Dispatchers.IO) {
                authService.loginWithPhone(phoneNumber, activity, callback)
            }

            _isLoading.value = false
        }
    }

    fun verifyCode(
        phoneCode: String,
        onSuccessVerification: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            if (verificationCode.isEmpty())
                return@launch

            val result = withContext(Dispatchers.IO) {
                try {
                    authService.verifyCode(verificationCode, phoneCode)
                } catch (ex: Exception) {
                    Result.failure(ex)
                }
            }

            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    onSuccessVerification()
                    _loginError.emit(null)
                }

            } else {
                _loginError.emit(result.exceptionOrNull()?.message)
                onError(result.exceptionOrNull() ?: Exception("Error"))
            }

            _isLoading.value = false
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            val user = userRepository.signInAnonymously()
            if (user != null) {
                _authState.value = Resource.Success(user)
            } else {
                _authState.value = Resource.Error("Error")
            }
        }
    }
}