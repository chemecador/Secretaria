package com.chemecador.secretaria.ui.viewmodel.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chemecador.secretaria.R
import com.chemecador.secretaria.data.local.UserPreferences
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences,
    private val res: ResourceProvider
) : ViewModel() {

    val email = MutableLiveData<String?>()
    private val _themeMode = MutableLiveData<String>()
    val themeMode: LiveData<String> = _themeMode

    init {
        viewModelScope.launch {
            userRepository.userEmail.collect { id ->
                email.postValue(id)
            }

            userPreferences.themeMode.collect { mode ->
                _themeMode.postValue(mode)
                applyTheme(mode)
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
            _themeMode.postValue(mode)
        }
    }

    private fun applyTheme(mode: String) {
        val themeValues = res.getStringArray(R.array.theme_values)
        when (mode) {
            themeValues[0] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) // "system"
            themeValues[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // "light"
            themeValues[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // "dark"
        }
    }

    fun signOut() {
        viewModelScope.launch {
            userRepository.clearUserDetails()
            auth.signOut()
        }
    }
}
