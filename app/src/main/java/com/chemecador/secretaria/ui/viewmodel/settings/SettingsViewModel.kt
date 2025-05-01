package com.chemecador.secretaria.ui.viewmodel.settings

import androidx.lifecycle.ViewModel
import com.chemecador.secretaria.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    internal val userRepository: UserRepository
) : ViewModel()
