package com.chemecador.secretaria.data.repositories

import com.chemecador.secretaria.data.local.UserPreferences
import com.chemecador.secretaria.data.services.AuthService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userPreferences: UserPreferences,
    private val authService: AuthService
) {

    val userId: Flow<String?> = flow {
        val user = authService.getUser()
        if (user != null) {
            emit(user.uid)
        } else {
            emit(null)
        }
    }

    val userEmail: Flow<String?> = flow {
        val user = authService.getUser()
        if (user != null) {
            emit(user.email)
        } else {
            emit(null)
        }
    }

    suspend fun clearUserDetails() {
        userPreferences.clearUserDetails()
    }

    suspend fun dontShowAgain() {
        userPreferences.dontShowAgain()
    }

    fun shouldShowWelcomeMessage() = userPreferences.shouldShowWelcomeMessage()


    fun getUserId(): String? {
        return authService.getUser()?.uid
    }
    fun getUsername(): String? {
        val user = authService.getUser()
        return user?.email ?: user?.displayName ?: user?.phoneNumber
    }

    suspend fun getColor(): Int? {
        return userPreferences.noteColor.firstOrNull()
    }
}