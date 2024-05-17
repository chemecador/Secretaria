package com.chemecador.secretaria.utils.log

import android.content.Context
import android.util.Log
import com.chemecador.secretaria.utils.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


class FileLoggingTree @Inject constructor(@ApplicationContext private val context: Context) :
    Timber.Tree() {

    private var currentLogFile: File? = null


    @Synchronized
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val logLevel = when (priority) {
            Log.VERBOSE -> "Verbose"
            Log.DEBUG -> "Debug"
            Log.INFO -> "Info"
            Log.WARN -> "Warning"
            Log.ERROR -> "Error"
            Log.ASSERT -> "Assert"
            else -> "Unknown"
        }

        val stackTrace = Throwable().stackTrace
        val fullClassName = if (stackTrace.size > 6) stackTrace[5].className else "UnknownClass"
        val simpleClassName = fullClassName.substringAfterLast('.')

        val effectiveTag = tag ?: simpleClassName
        val logMessage =
            "${DateUtils.getCurrentDateTimeMillis()} - $logLevel - $effectiveTag: $message"
        writeFile(logMessage, t)
    }

    private fun writeFile(logMessage: String, t: Throwable?) {
        val currentDate = DateUtils.getCurrentDay()
        if (currentLogFile == null || !currentLogFile!!.name.startsWith(currentDate)) {
            currentLogFile = getLogFile(currentDate)
        }
        try {
            FileWriter(currentLogFile, true).use { fw ->
                fw.append(logMessage + "\n")
                if (t != null) {
                    fw.append(t.stackTraceToString() + "\n")
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "Error writing log to file")
        }
    }

    private fun getLogFile(date: String): File {
        val fileName = "$date.txt"
        val logDirectory = File(context.filesDir, "logs")
        if (!logDirectory.exists()) {
            logDirectory.mkdirs()
        }
        val logFile = File(logDirectory, fileName)
        if (!logFile.exists()) {
            try {
                logFile.createNewFile()
            } catch (e: IOException) {
                Timber.e(e, "Failed to create log file")
            }
        }
        return logFile
    }

    companion object {
        fun deleteOldLogs(context: Context) {
            val logFolder = File(context.filesDir, "logs")
            val limitDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, -7)
            }.time

            logFolder.listFiles()?.forEach { file ->
                val fileDate = SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES")).parse(
                    file.name.substring(
                        0,
                        10
                    )
                )
                if (fileDate != null && fileDate.before(limitDate)) {
                    file.delete()
                }
            }
        }
    }
}