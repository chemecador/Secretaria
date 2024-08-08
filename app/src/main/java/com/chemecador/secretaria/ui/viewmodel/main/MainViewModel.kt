package com.chemecador.secretaria.ui.viewmodel.main

import androidx.lifecycle.ViewModel
import com.chemecador.secretaria.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val themeMode: Flow<String> = userRepository.themeMode

    fun isAnonymousUser(): Boolean {
        return userRepository.isAnonymousUser()
    }
}
