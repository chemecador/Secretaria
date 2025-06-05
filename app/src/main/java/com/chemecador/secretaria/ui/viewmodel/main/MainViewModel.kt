package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.data.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val rep: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    fun signOut() {
        viewModelScope.launch {
            rep.clearUserDetails()
            auth.signOut()
        }
    }
}