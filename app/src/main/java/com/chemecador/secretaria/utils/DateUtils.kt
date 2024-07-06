package com.chemecador.secretaria.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateUtils {

    private fun getCurrentDayFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    private fun getCurrentDateTimeFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    fun getCurrentDateTimeMillis(): String {
        val currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Madrid"))
        return currentDateTime.format(getCurrentDateTimeFormatter())
    }

    fun getCurrentDay(): String {
        val currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Madrid"))
        return currentDateTime.format(getCurrentDayFormatter())
    }

    fun formatDetailed(timestamp: Timestamp): String {
        val date: Date = timestamp.toDate()
        val sdf = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

    fun formatSimple(timestamp: Timestamp): String {
        val date: Date = timestamp.toDate()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(date)
    }
}
