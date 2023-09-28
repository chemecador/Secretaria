package com.chemecador.secretaria.logger

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 * Clase para loggear lo que pasa en la aplicación.<br></br>
 *
 * Envia todo a android.util.Log *y* a un fichero para poder revisarlo más tarde<br></br>
 *
 * Implementa parcialmente las mismas funciones que `android.util.Log`, con la intención
 * de ser usada como reemplazamiento
 */
class Logger(context: Context) {
    /**
     * Abre un fichero con la fecha de hoy como nombre
     */
    @SuppressLint("SetWorldReadable")
    private fun abrirFichero() {
        @SuppressLint("SimpleDateFormat") val filename = SimpleDateFormat("yyyy-MM-dd").format(
            Calendar.getInstance().time
        ) + ".txt"
        val rootDirectory = context.filesDir.absolutePath
        val path = "/logs/"
        val fichero = File(rootDirectory + path + filename)
        File(rootDirectory + path).mkdirs()
        file = fichero
        file!!.setReadable(true, false) //Todo el mundo puede leer los logs
        try {
            fos = FileOutputStream(file, true)
        } catch (x: IOException) {
            Log.e("Logger", "No se pudo abrir el fichero de log para escritura", x)
            try {
                file!!.createNewFile()
            } catch (x2: IOException) {
                Log.e("Logger", "Tampoco se puede crear el fichero", x2)
            }
        }
    }

    /**
     * Escribe una línea al fichero vinculado a este `Logger` con la hora actual incluida
     * @param linea La línea a escribir
     */
    @Synchronized
    private fun escribirLinea(linea: String) {
        @SuppressLint("SimpleDateFormat") val lineaFinal = """
             [${SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().time)}]$linea
             
             """.trimIndent()
        try {
            fos!!.write(lineaFinal.toByteArray(StandardCharsets.UTF_8))
            fos!!.flush()
        } catch (x: IOException) {
            Log.e("Logger", "No se pudo escribir al fichero de log", x)
        }
    }

    private val context: Context
    var file: File? = null
        private set
    private var fos: FileOutputStream? = null

    /**
     * Crea un nuevo `Logger` con el contexto especificado
     */
    init {
        this.context = context.applicationContext
        abrirFichero()
        iU("--------------------", "**** Nuevo inicio de sesión ****\n\n\n")
    }
    /**
     * Loguea un mensaje de INFORMACIÓN, usando la instancia de `Logger` actual en vez del singleton
     */
    /**
     * Loguea un mensaje de INFORMACIÓN, usando la instancia de `Logger` actual en vez del singleton
     */
    @JvmOverloads
    fun iU(className: String, message: String, error: Throwable? = null) {
        Log.i(className, message, error)
        escribirLinea("[INFO/$className] $message")
        if (error != null) {
            val ps = PrintStream(fos)
            error.printStackTrace(ps)
            ps.flush()
        }
    }
    /**
     * Loguea un mensaje de ERROR, usando la instancia de `Logger` actual en vez del singleton
     */
    /**
     * Loguea un mensaje de ERROR, usando la instancia de `Logger` actual en vez del singleton
     */
    @JvmOverloads
    fun eU(className: String, message: String, error: Throwable? = null) {
        Log.e(className, message, error)
        escribirLinea("[ERROR/$className] $message")
        if (error != null) {
            val ps = PrintStream(fos)
            error.printStackTrace(ps)
            ps.flush()
        }
    }
    /**
     * Loguea un mensaje de DEBUG, usando la instancia de `Logger` actual en vez del singleton
     */
    /**
     * Loguea un mensaje de DEBUG, usando la instancia de `Logger` actual en vez del singleton
     */
    @JvmOverloads
    fun dU(className: String, message: String, error: Throwable? = null) {
        Log.d(className, message, error)
        escribirLinea("[DEBUG/$className] $message")
        if (error != null) {
            val ps = PrintStream(fos)
            error.printStackTrace(ps)
            ps.flush()
        }
    }
    /**
     * Loguea un mensaje de WARNING, usando la instancia de `Logger` actual en vez del singleton
     */
    /**
     * Loguea un mensaje de WARNING, usando la instancia de `Logger` actual en vez del singleton
     */
    @JvmOverloads
    fun wU(className: String, message: String, error: Throwable? = null) {
        Log.w(className, message, error)
        escribirLinea("[WARN/$className] $message")
        if (error != null) {
            val ps = PrintStream(fos)
            error.printStackTrace(ps)
            ps.flush()
        }
    }
    /**
     * Loguea un mensaje de VERBOSE, usando la instancia de `Logger` actual en vez del singleton
     */
    /**
     * Loguea un mensaje de VERBOSE, usando la instancia de `Logger` actual en vez del singleton
     */
    @JvmOverloads
    fun vU(className: String, message: String, error: Throwable? = null) {
        Log.v(className, message, error)
        escribirLinea("[VERBOSE/$className] $message")
        if (error != null) {
            val ps = PrintStream(fos)
            error.printStackTrace(ps)
            ps.flush()
        }
    }
    /**
     * Loguea un mensaje de WHAT A TERRIBLE FAILURE, usando la instancia de `Logger` actual en vez del singleton
     */
    /**
     * Loguea un mensaje de WHAT A TERRIBLE FAILURE, usando la instancia de `Logger` actual en vez del singleton
     */
    @JvmOverloads
    fun wtfU(className: String, message: String, error: Throwable? = null) {
        Log.wtf(className, message, error)
        escribirLinea("[TERRIBLE/$className] $message")
        if (error != null) {
            val ps = PrintStream(fos)
            error.printStackTrace(ps)
            ps.flush()
        }
    }

    companion object {
        private const val DIAS_ELIMINAR = 7
        var singleton: Logger? = null
            private set

        /**
         * Crea un singleton para ser accedido después por las funciones estáticas de esta clase
         * @param context El contexto de la aplicación
         * @return La instancia de `Logger` creada
         */
        @JvmStatic
        fun crearSingleton(context: Context): Logger? {
            singleton = Logger(context)
            deleteOldLogs(context, DIAS_ELIMINAR)
            return singleton
        }

        /**
         * Método que elimina los Logs de hace más de [diasEliminar] días
         * @param context contexto de la aplicación
         * @param diasEliminar días que guarda los logs antes de eliminarlos
         */
        @SuppressLint("SimpleDateFormat")
        private fun deleteOldLogs(context: Context, diasEliminar: Int) {
            // Obtener la carpeta donde se almacenan los archivos de log
            val logFolder = File(context.filesDir.absolutePath + "/logs/")

            // Obtener la fecha actual
            val currentDate = Calendar.getInstance()

            // Restar 7 días a la fecha actual para obtener la fecha límite
            currentDate.add(Calendar.DAY_OF_MONTH, diasEliminar * -1)
            val limitDate = currentDate.time

            // Recorrer todos los archivos de log en la carpeta
            val logFiles = logFolder.listFiles()!!
            for (logFile in logFiles) {
                val fileName = logFile.name

                // Obtener la fecha del archivo de log
                val dateString = fileName.substring(0, 10)
                var fileDate: Date? = null
                try {
                    fileDate = SimpleDateFormat("yyyy-MM-dd").parse(dateString)
                } catch (e: ParseException) {
                    Log.e("Logger", "No se pudo abrir el fichero de log para escritura", e)
                }

                // Si la fecha del archivo de log es anterior a la fecha límite, eliminar el archivo
                if (fileDate != null && fileDate.before(limitDate)) {
                    logFile.delete()
                }
            }
        }

        /**
         * Loguea un mensaje de INFORMACIÓN
         */
        fun i(className: String, message: String) {
            i(className, message, null)
        }

        /**
         * Loguea un mensaje de INFORMACIÓN
         */
        fun i(className: String, message: String, error: Throwable?) {
            Log.i(className, message, error)
            singleton!!.escribirLinea("[INFO/$className] $message")
            if (error != null) {
                val ps = PrintStream(singleton!!.fos)
                error.printStackTrace(ps)
                ps.flush()
            }
        }

        /**
         * Loguea un mensaje de ERROR
         */
        @JvmStatic
        fun e(className: String, message: String) {
            e(className, message, null)
        }

        /**
         * Loguea un mensaje de ERROR
         */
        @JvmStatic
        fun e(className: String, message: String, error: Throwable?) {
            Log.e(className, message, error)
            singleton!!.escribirLinea("[ERROR/$className] $message")
            if (error != null) {
                val ps = PrintStream(singleton!!.fos)
                error.printStackTrace(ps)
                ps.flush()
            }
        }

        /**
         * Loguea un mensaje de ERROR, mostrando un `CustomToast` con el mensaje proporcionado también
         */
        fun eT(className: String, message: String, context: Context) {
            eT(className, message, null, context)
        }

        /**
         * Loguea un mensaje de ERROR, mostrando un `CustomToast` con el mensaje proporcionado también
         */
        fun eT(className: String, message: String, error: Throwable?, context: Context) {
            Log.e(className, message, error)
            singleton!!.escribirLinea("[ERROR/$className] $message")
            if (error != null) {
                val ps = PrintStream(singleton!!.fos)
                error.printStackTrace(ps)
                ps.flush()
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        /**
         * Loguea un mensaje de DEBUG
         */
        fun d(className: String, message: String) {
            d(className, message, null)
        }

        /**
         * Loguea un mensaje de DEBUG
         */
        fun d(className: String, message: String, error: Throwable?) {
            Log.d(className, message, error)
            singleton!!.escribirLinea("[DEBUG/$className] $message")
            if (error != null) {
                val ps = PrintStream(singleton!!.fos)
                error.printStackTrace(ps)
                ps.flush()
            }
        }

        /**
         * Loguea un mensaje de WARNING
         */
        fun w(className: String, message: String) {
            w(className, message, null)
        }

        /**
         * Loguea un mensaje de WARNING
         */
        fun w(className: String, message: String, error: Throwable?) {
            Log.w(className, message, error)
            singleton!!.escribirLinea("[WARN/$className] $message")
            if (error != null) {
                val ps = PrintStream(singleton!!.fos)
                error.printStackTrace(ps)
                ps.flush()
            }
        }

        /**
         * Loguea un mensaje de VERBOSE
         */
        fun v(className: String, message: String) {
            v(className, message, null)
        }

        /**
         * Loguea un mensaje de VERBOSE
         */
        fun v(className: String, message: String, error: Throwable?) {
            Log.v(className, message, error)
            singleton!!.escribirLinea("[VERBOSE/$className] $message")
            if (error != null) {
                val ps = PrintStream(singleton!!.fos)
                error.printStackTrace(ps)
                ps.flush()
            }
        }

        /**
         * Loguea un mensaje de WHAT A TERRIBLE FAILURE
         */
        fun wtf(className: String, message: String) {
            wtf(className, message, null)
        }

        /**
         * Loguea un mensaje de WHAT A TERRIBLE FAILURE
         */
        fun wtf(className: String, message: String, error: Throwable?) {
            Log.wtf(className, message, error)
            singleton!!.escribirLinea("[TERRIBLE/$className] $message")
            if (error != null) {
                val ps = PrintStream(singleton!!.fos)
                error.printStackTrace(ps)
                ps.flush()
            }
        }
    }
}