package com.chemecador.secretaria.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    val userId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }

    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL_KEY]
        }


    suspend fun saveUserDetails(userId: String, email: String?) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            email?.let { preferences[USER_EMAIL_KEY] = it }
        }
    }

    suspend fun clearUserDetails() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
        }
    }
}
