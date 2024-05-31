package com.chemecador.secretaria.ui.viewmodel.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {


    val email = MutableLiveData<String?>()

    init {
        viewModelScope.launch {
            userRepository.userEmail.collect { id ->
                email.postValue(id)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            userRepository.clearUserDetails()
            auth.signOut()
        }
    }
}