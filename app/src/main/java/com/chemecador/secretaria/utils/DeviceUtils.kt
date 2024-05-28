package com.chemecador.secretaria.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


object DeviceUtils {

    fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val vista = activity.currentFocus ?: View(activity)
        inputMethodManager.hideSoftInputFromWindow(vista.windowToken, 0)
    }

}