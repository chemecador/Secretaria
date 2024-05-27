package com.chemecador.secretaria.ui.viewmodel.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _pfpUri = MutableStateFlow<Uri?>(null)
    val pfpUri: StateFlow<Uri?> = _pfpUri.asStateFlow()

    init {
        loadUserPhotoUrl()
    }

    private fun loadUserPhotoUrl() {
        _pfpUri.value = firebaseAuth.currentUser?.photoUrl
    }
}
