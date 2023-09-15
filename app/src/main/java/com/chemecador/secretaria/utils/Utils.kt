package com.chemecador.secretaria.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.chemecador.secretaria.gui.CustomToast
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
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

    val simpleFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val fullFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val fullSpanishFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    val simpleSpanishFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun beautifyDate(date: String?): String {
        val dateTime = LocalDateTime.parse(date, fullFormatter)
        val newFormat: DateTimeFormatter = if (dateTime.hour == 0 && dateTime.minute == 0 && dateTime.second == 0) {
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        } else {
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        }
        val newDate = LocalDateTime.parse(date, fullFormatter)
        return newDate.format(newFormat)
    }
    fun beautifySpanishDate(date: String?): String {
        val fullFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        val dateTime = LocalDateTime.parse(date, fullFormat)
        val newFormat: DateTimeFormatter = if (dateTime.hour == 0 && dateTime.minute == 0 && dateTime.second == 0) {
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        } else {
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        }
        val newDate = LocalDateTime.parse(date, fullFormat)
        return newDate.format(newFormat)
    }
    fun beautifyDate(unixTimestamp: Long): String {

        // Convertir el tiempo Unix a un objeto Instant
        val instant = Instant.ofEpochSecond(unixTimestamp)

        // Convertir el Instant a una fecha y hora en una zona horaria específica (por ejemplo, UTC)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))

        // Define los formatos de fecha y hora
        val fullFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

        // Determina cuál formato utilizar en función de la hora, minutos y segundos
        val selectedFormat = if (dateTime.hour == 0 && dateTime.minute == 0 && dateTime.second == 0) {
            dateFormat
        } else {
            dateTimeFormat
        }

        // Formatea la fecha y hora según el formato seleccionado
        return dateTime.format(selectedFormat)
    }

    fun beautifyDate(localDateTime: LocalDateTime): String {

        // Define los formatos de fecha y hora
        val fullFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

        // Determina cuál formato utilizar en función de la hora, minutos y segundos
        val selectedFormat = if (localDateTime.hour == 0 && localDateTime.minute == 0 && localDateTime.second == 0) {
            dateFormat
        } else {
            dateTimeFormat
        }

        // Formatea la fecha y hora según el formato seleccionado
        return localDateTime.format(selectedFormat)
    }
}