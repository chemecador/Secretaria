package com.chemecador.secretaria.data.repositories

import com.chemecador.secretaria.data.local.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userPreferences: UserPreferences
) {

    val userId: Flow<String?> = userPreferences.userId
    val userEmail: Flow<String?> = userPreferences.userEmail

    suspend fun saveUserDetails(userId: String, email: String?) {
        userPreferences.saveUserDetails(userId, email)
    }

    suspend fun clearUserDetails() {
        userPreferences.clearUserDetails()
    }


    suspend fun dontShowAgain() {
        userPreferences.dontShowAgain()
    }

    fun shouldShowWelcomeMessage() = userPreferences.shouldShowWelcomeMessage()
}
