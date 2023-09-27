package com.chemecador.secretaria.utils

import android.content.Context
import androidx.preference.PreferenceManager

object PreferencesHandler {
    const val PREF_TOKEN = "token"
    const val PREF_ID = "id"
    const val PREF_ONLINE = "online"
    const val PREF_LAST_LOGIN_OK = "last_login_ok"
    fun save(context: Context?, id: Int, token: String?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(
            context!!
        )
        val editor = prefs.edit()
        editor.putString(PREF_TOKEN, token)
        editor.putInt(PREF_ID, id)
        editor.putBoolean(PREF_ONLINE, true)
        editor.putBoolean(PREF_LAST_LOGIN_OK, true)
        editor.apply()
    }

    fun clear(context: Context?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(
            context!!
        )
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    fun getToken(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_TOKEN, "")!!
    }

    fun getId(context: Context): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_ID, 0)
    }

    fun isOnline(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(PREF_ONLINE, false)
    }

    fun lastLoginOk(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(PREF_LAST_LOGIN_OK, false)
    }

    fun isTokenValid(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return (prefs.contains(PREF_TOKEN)
                && prefs.getString(PREF_TOKEN, "")!!.isNotEmpty()
                && prefs.getBoolean(PREF_LAST_LOGIN_OK, false))
    }

    fun getString(context: Context, stringName: String?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(stringName, "")
    }

    fun getInt(context: Context, intName: String?): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(intName, -1)
    }

    fun getBoolean(context: Context, booleanName: String?): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(booleanName, false)
    }

    fun getFloat(context: Context, floatName: String?): Float {
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(floatName, -1f)
    }

    fun putInt(context: Context, intName: String, intValue: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(intName, intValue).apply()
    }

    fun putBoolean(context: Context, booleanName: String, booleanValue: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(booleanName, booleanValue).apply()
    }

    fun putString(context: Context, stringName: String, stringValue: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(stringName, stringValue).apply()
    }

    fun putFloat(context: Context, floatName: String, floatValue: Float) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat(floatName, floatValue).apply()
    }

}