package com.chemecador.secretaria.utils

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DateUtils {


    private fun getCurrentDayFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    private fun getSpanishDateFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    private fun getCurrentDateTimeFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    fun getCurrentDateTimeMillisFormatter(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    }


    fun getCurrentDateTimeMillis(): String {
        val currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Madrid"))
        return currentDateTime.format(getCurrentDateTimeFormatter())
    }

    fun getCurrentDateTime(): String {
        val currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Madrid"))
        return currentDateTime.format(getCurrentDateTimeFormatter())
    }

    fun getCurrentDay(): String {
        val currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Madrid"))
        return currentDateTime.format(getCurrentDayFormatter())
    }

    fun getSpanishDate(): String {
        val currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Madrid"))
        return currentDateTime.format(getSpanishDateFormatter())
    }

}