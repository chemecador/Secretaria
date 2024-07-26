package com.chemecador.secretaria.data.local

import android.content.Context
import android.graphics.Color
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chemecador.secretaria.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("secretaria")

class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USERCODE_KEY = stringPreferencesKey("user_code")
        private val SHOW_WELCOME_MESSAGE_KEY = booleanPreferencesKey("show_welcome_message")
        private val THEME_KEY = stringPreferencesKey("theme_mode")
        private val NOTE_COLOR_KEY = intPreferencesKey("note_color")
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
            preferences.remove(USERCODE_KEY)
        }
    }

    suspend fun dontShowAgain() {
        context.dataStore.edit { preferences ->
            preferences[SHOW_WELCOME_MESSAGE_KEY] = false
        }
    }

    fun shouldShowWelcomeMessage(): Flow<Boolean> {
        return context.dataStore.data
            .map { preferences ->
                preferences[SHOW_WELCOME_MESSAGE_KEY] ?: true
            }
    }

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_KEY] ?: context.resources.getStringArray(R.array.theme_options)[0]
        }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode
        }
    }

    val noteColor: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[NOTE_COLOR_KEY] ?: Color.MAGENTA // Color por defecto
        }

    suspend fun saveNoteColor(color: Int) {
        context.dataStore.edit { preferences ->
            preferences[NOTE_COLOR_KEY] = color
        }
    }


    val userCodeFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USERCODE_KEY]
        }

    suspend fun saveUserCode(userCode: String) {
        context.dataStore.edit { preferences ->
            preferences[USERCODE_KEY] = userCode
        }
    }
}
