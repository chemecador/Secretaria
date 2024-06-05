package com.chemecador.secretaria.ui.viewmodel.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.network.services.AuthService
import com.chemecador.secretaria.data.repositories.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
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

    private var verificationCode: String = ""
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
                                userRepository.saveUserDetails(
                                    userId = firebaseUser.uid,
                                    email = firebaseUser.email.orEmpty()
                                )

                                onVerificationComplete()
                                _loginError.emit(null)
                            }
                        } else {
                            _loginError.emit(result.exceptionOrNull()?.message)
                            Timber.e(result.exceptionOrNull())
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


    fun verifyCode(phoneCode: String, onSuccessVerification: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            if (verificationCode.isEmpty())
                return@launch

            val result = withContext(Dispatchers.IO) {
                authService.verifyCode(verificationCode, phoneCode)
            }

            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()
                if (firebaseUser != null) {
                    userRepository.saveUserDetails(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email.orEmpty()
                    )
                    onSuccessVerification()
                    _loginError.emit(null)
                }
            } else {
                _loginError.emit(result.exceptionOrNull()?.message)
                Timber.e(result.exceptionOrNull())
            }

            _isLoading.value = false
        }
    }

}