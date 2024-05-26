package com.chemecador.secretaria.ui.viewmodel.login

import androidx.lifecycle.ViewModel
import com.chemecador.secretaria.data.network.services.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(private val authService: AuthService) : ViewModel() {

    fun checkRoute() =
        if (authService.getUser() == null) {
            Route.Login
        } else {
            Route.Main
        }
}

sealed class Route {
    data object Login : Route()
    data object Main : Route()
}