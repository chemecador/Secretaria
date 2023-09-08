package com.chemecador.secretaria.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class PreferencesHandler {

    public static final String PREF_TOKEN = "token";
    public static final String PREF_ID = "id";
    public static final String PREF_ONLINE = "online";
    public static final String PREF_LAST_LOGIN_OK = "last_login_ok";


    public static void save(Context context, int id, String token) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_TOKEN, token);
        editor.putInt(PREF_ID, id);
        editor.putBoolean(PREF_ONLINE, true);
        editor.putBoolean(PREF_LAST_LOGIN_OK, true);
        editor.apply();
    }

    public static void clear(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public static String getToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_TOKEN, "");
    }

    public static int getId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(PREF_ID, 0);

    }

    public static boolean isOnline(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_ONLINE, false);
    }

    public static boolean isTokenValid(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.contains(PREF_TOKEN)
                && !prefs.getString(PREF_TOKEN, "").isEmpty()
                && prefs.getBoolean(PREF_LAST_LOGIN_OK, false);
    }

    public static String getString(Context context, String stringName){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(stringName, "");
    }

    public static int getInt(Context context, String intName){
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(intName, -1);
    }

    public static boolean getBoolean(Context context, String booleanName){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(booleanName, false);
    }

    public static float getFloat(Context context, String floatName){
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat(floatName, -1f);
    }
}

