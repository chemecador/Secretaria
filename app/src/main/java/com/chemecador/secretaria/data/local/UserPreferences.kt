package com.chemecador.secretaria.data.local

import android.content.Context
import android.graphics.Color
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
        private val USERCODE_KEY = stringPreferencesKey("user_code")
        private val THEME_KEY = stringPreferencesKey("theme_mode")
        private val NOTE_COLOR_KEY = intPreferencesKey("note_color")
    }

    suspend fun clearUserDetails() {
        context.dataStore.edit { preferences ->
            preferences.remove(USERCODE_KEY)
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
            preferences[NOTE_COLOR_KEY] ?: Color.GREEN
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
