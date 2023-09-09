package com.chemecador.secretaria.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.chemecador.secretaria.gui.CustomToast
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Utils {
    const val SUCCESS = 1
    const val INFO = 2
    const val WARNING = 3
    const val ERROR = 4
    fun showToast(context: Context, type: Int, message: String?) {
        (context as Activity).runOnUiThread {
            CustomToast(
                context,
                type,
                Toast.LENGTH_LONG
            ).show(message)
        }
    }

    fun showToast(context: Context, type: Int, resource: Int) {
        (context as Activity).runOnUiThread {
            CustomToast(
                context,
                type,
                Toast.LENGTH_LONG
            ).show(context.getString(resource))
        }
    }

    val dayFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val fullFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val fullBeautyFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    val dayBeautyFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun beautifyDate(date: String?): String {
        val fullFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime = LocalDateTime.parse(date, fullFormat)
        val newFormat: DateTimeFormatter
        newFormat = if (dateTime.hour == 0 && dateTime.minute == 0 && dateTime.second == 0) {
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        } else {
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        }
        val newDate = LocalDateTime.parse(date, fullFormat)
        return newDate.format(newFormat)
    }
}