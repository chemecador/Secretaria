package com.chemecador.secretaria.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.preference.PreferenceManager
import com.chemecador.secretaria.logger.Logger

object PreferencesHandler {
    const val PREF_TOKEN = "token"
    const val PREF_ID = "id"
    const val PREF_ONLINE = "online"
    const val PREF_LAST_LOGIN_OK = "last_login_ok"

    // Users
    private const val PREF_USERS = "users"
    const val PREF_NEW_USER = "new_user"
    const val PREF_NEW_VERSION = "new_version"
    const val PREF_LAST_VERSION = "last_version"


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
        val lastLoginOk = getBoolean(context, PREF_LAST_LOGIN_OK);
        putBoolean(context, PREF_LAST_LOGIN_OK, false)
        return lastLoginOk
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
    fun getBoolean(context: Context, booleanName: String?, booleanDefault: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(booleanName, booleanDefault)
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

    fun isNewUser(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_USERS, Context.MODE_PRIVATE)
        val isNewUser = prefs.getBoolean(PREF_NEW_USER, true)
        prefs.edit().putBoolean(PREF_NEW_USER, false).apply()
        return isNewUser
    }

    fun isNewVersion(context: Context) : Boolean {
        val prefs = context.getSharedPreferences(PREF_USERS, Context.MODE_PRIVATE)
        val lastVersionCode: Long = prefs.getLong(PREF_LAST_VERSION, 1)
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersionCode: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
            prefs.edit().putLong(PREF_LAST_VERSION, currentVersionCode).apply()
            return currentVersionCode > lastVersionCode
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.e("PreferencesHandler", "Error al mostrar el historial de versiones", e)
        }
        return false
    }
    /*fun isNewVersion(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_USERS, Context.MODE_PRIVATE)
        val isNewUser = prefs.getBoolean(PREF_NEW_VERSION, true)
        prefs.edit().putBoolean(PREF_NEW_VERSION, false).apply()
        return isNewUser
    }*/

}