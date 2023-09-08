package com.chemecador.secretaria.logger;

import static com.chemecador.secretaria.utils.Utils.ERROR;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.chemecador.secretaria.gui.CustomToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Clase para loggear lo que pasa en la aplicación.<br/>
 *
 * Envia todo a android.util.Log <i>y</i> a un fichero para poder revisarlo más tarde<br />
 *
 * Implementa parcialmente las mismas funciones que <code>android.util.Log</code>, con la intención
 * de ser usada como reemplazamiento
 */
public class Logger {

    /**
     * Abre un fichero con la fecha de hoy como nombre
     */
    @SuppressLint("SetWorldReadable")
    private void abrirFichero() {
        @SuppressLint("SimpleDateFormat") String filename = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime())+".txt";
        String rootDirectory = context.getFilesDir().getAbsolutePath();
        String path = "/logs/";
        File fichero = new File(rootDirectory+path+filename);
        new File(rootDirectory+path).mkdirs();
        file = fichero;
        file.setReadable(true, false); //Todo el mundo puede leer los logs
        try {
            fos = new FileOutputStream(file, true);
        } catch (IOException x) {
            Log.e("Logger", "No se pudo abrir el fichero de log para escritura", x);
            try {
                file.createNewFile();
            } catch (IOException x2) {
                Log.e("Logger", "Tampoco se puede crear el fichero", x2);
            }
        }
    }

    /**
     * Escribe una línea al fichero vinculado a este <code>Logger</code> con la hora actual incluida
     * @param linea La línea a escribir
     */
    private synchronized void escribirLinea(String linea) {
        @SuppressLint("SimpleDateFormat") String lineaFinal = "["+new SimpleDateFormat("HH:mm:ss.SSS").format(Calendar.getInstance().getTime())+"]"
                +linea+"\n";
        try {
            fos.write(lineaFinal.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (IOException x) {
            Log.e("Logger", "No se pudo escribir al fichero de log", x);
        }
    }

    public static Logger getSingleton() {
        return singleton;
    }

    public File getFile() {
        return file;
    }

    private final Context context;
    private File file;
    private FileOutputStream fos;
    private static final int DIAS_ELIMINAR = 7;

    private static Logger singleton;

    /**
     * Crea un nuevo <code>Logger</code> con el contexto especificado
     * @param context El contexto de la aplicación, necesario para elegir el directorio de ficheros.
     */
    public Logger(Context context) {
        this.context = context.getApplicationContext();
        abrirFichero();
        this.iU("--------------------", "**** Nuevo inicio de sesión ****\n\n\n");    }

    /**
     * Crea un singleton para ser accedido después por las funciones estáticas de esta clase
     * @param context El contexto de la aplicación
     * @return La instancia de <code>Logger</code> creada
     */
    public static Logger crearSingleton(Context context) {
        singleton = new Logger(context);
        deleteOldLogs(context, Logger.DIAS_ELIMINAR);
        return singleton;
    }

    /**
     * Método que elimina los Logs de hace más de [diasEliminar] días
     * @param context contexto de la aplicación
     * @param diasEliminar días que guarda los logs antes de eliminarlos
     */
    @SuppressLint("SimpleDateFormat")
    private static void deleteOldLogs(Context context, int diasEliminar) {
        // Obtener la carpeta donde se almacenan los archivos de log
        File logFolder = new File(context.getFilesDir().getAbsolutePath() + "/logs/");

        // Obtener la fecha actual
        Calendar currentDate = Calendar.getInstance();

        // Restar 7 días a la fecha actual para obtener la fecha límite
        currentDate.add(Calendar.DAY_OF_MONTH, diasEliminar * -1);
        Date limitDate = currentDate.getTime();

        // Recorrer todos los archivos de log en la carpeta
        File[] logFiles = logFolder.listFiles();
        assert logFiles != null;
        for (File logFile : logFiles) {
            String fileName = logFile.getName();

            // Obtener la fecha del archivo de log
            String dateString = fileName.substring(0, 10);
            Date fileDate = null;
            try {
                fileDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
            } catch (ParseException e) {
                Log.e("Logger", "No se pudo abrir el fichero de log para escritura", e);
            }

            // Si la fecha del archivo de log es anterior a la fecha límite, eliminar el archivo
            if (fileDate != null && fileDate.before(limitDate)) {
                logFile.delete();
            }
        }
    }

    /**
     * Loguea un mensaje de INFORMACIÓN
     */
    public static void i(String tag, String message) {
        Logger.i(tag, message, null);
    }

    /**
     * Loguea un mensaje de INFORMACIÓN
     */
    public static void i(String tag, String message, Throwable error) {
        Log.i(tag, message, error);

        singleton.escribirLinea("[INFO/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(singleton.fos);
            error.printStackTrace(ps);
            ps.flush();
        }
    }

    /**
     * Loguea un mensaje de INFORMACIÓN, usando la instancia de <code>Logger</code> actual en vez del singleton
     */
    public void iU(String tag, String message) {
        this.iU(tag, message, null);
    }

    /**
     * Loguea un mensaje de INFORMACIÓN, usando la instancia de <code>Logger</code> actual en vez del singleton
     */
    public void iU(String tag, String message, Throwable error) {
        Log.i(tag, message, error);

        this.escribirLinea("[INFO/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(this.fos);
            error.printStackTrace(ps);
            ps.flush();
        }
    }


    /**
     * Loguea un mensaje de ERROR
     */
    public static void e(String tag, String message) {
        Logger.e(tag, message, null);
    }

    /**
     * Loguea un mensaje de ERROR
     */
    public static void e(String tag, String message, Throwable error) {
        Log.e(tag, message, error);

        singleton.escribirLinea("[ERROR/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(singleton.fos);
            error.printStackTrace(ps);
            ps.flush();
        }
    }

    /**
     * Loguea un mensaje de ERROR, mostrando un <code>CustomToast</code> con el mensaje proporcionado también
     */
    public static void eT(String tag, String message, Context context) {
        Logger.eT(tag, message, null, context);
    }

    /**
     * Loguea un mensaje de ERROR, mostrando un <code>CustomToast</code> con el mensaje proporcionado también
     */
    public static void eT(String tag, String message, Throwable error, Context context) {
        Log.e(tag, message, error);

        singleton.escribirLinea("[ERROR/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(singleton.fos);
            error.printStackTrace(ps);
            ps.flush();
        }

        new CustomToast(context, ERROR, Toast.LENGTH_LONG)
                .show(message);
    }

    /**
     * Loguea un mensaje de ERROR, usando la instancia de <code>Logger</code> actual en vez del singleton
     */
    public void eU(String tag, String message) {
        this.eU(tag, message, null);
    }

    /**
     * Loguea un mensaje de ERROR, usando la instancia de <code>Logger</code> actual en vez del singleton
     */
    public void eU(String tag, String message, Throwable error) {
        Log.e(tag, message, error);

        this.escribirLinea("[ERROR/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(this.fos);
            error.printStackTrace(ps);
            ps.flush();
        }
    }


    /**
     * Loguea un mensaje de DEBUG
     */
    public static void d(String tag, String message) {
        Logger.d(tag, message, null);
    }

    /**
     * Loguea un mensaje de DEBUG
     */
    public static void d(String tag, String message, Throwable error) {
        Log.d(tag, message, error);

        singleton.escribirLinea("[DEBUG/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(singleton.fos);
            error.printStackTrace(ps);
            ps.flush();
        }
    }

    /**
     * Loguea un mensaje de DEBUG, usando la instancia de <code>Logger</code> actual en vez del singleton
     */
    public void dU(String tag, String message) {
        this.dU(tag, message, null);
    }

    /**
     * Loguea un mensaje de DEBUG, usando la instancia de <code>Logger</code> actual en vez del singleton
     */
    public void dU(String tag, String message, Throwable error) {
        Log.d(tag, message, error);

        this.escribirLinea("[DEBUG/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(this.fos);
            error.printStackTrace(ps);
            ps.flush();
        }
    }

    /**
     * Loguea un mensaje de WARNING
     */
    public static void w(String tag, String message) {
        Logger.w(tag, message, null);
    }

    /**
     * Loguea un mensaje de WARNING
     */
    public static void w(String tag, String message, Throwable error) {
        Log.w(tag, message, error);

        singleton.escribirLinea("[WARN/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(singleton.fos);
            error.printStackTrace(ps);
            ps.flush();
        }
    }

    /**
     * Loguea un mensaje de WARNING, usando la instancia de <code>Logger</code> actual en vez del singleton
     */
    public void wU(String tag, String message) {
        this.wU(tag, message, null);
    }

    /**
     * Loguea un mensaje de WARNING, usando la instancia de <code>Logger</code> actual en vez del singleton
     */
    public void wU(String tag, String message, Throwable error) {
        Log.w(tag, message, error);

        this.escribirLinea("[WARN/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(this.fos);
            error.printStackTrace(ps);
            ps.flush();
        }
    }

    /**
     * Loguea un mensaje de VERBOSE
     */
    public static void v(String tag, String message) {
        Logger.v(tag, message, null);
    }

    /**
     * Loguea un mensaje de VERBOSE
     */
    public static void v(String tag, String message, Throwable error) {
        Log.v(tag, message, error);

        singleton.escribirLinea("[VERBOSE/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(singleton.fos);
            error.printStackTrace(ps);
            ps.flush();
        }
    }

    /**
     * Loguea un mensaje de VERBOSE, usando la instancia de <code>Logger</code> actual en vez del singleton
     */
    public void vU(String tag, String message) {
        this.vU(tag, message, null);
    }

    /**
     * Loguea un mensaje de VERBOSE, usando la instancia de <code>Logger</code> actual en vez del singleton
     */
    public void vU(String tag, String message, Throwable error) {
        Log.v(tag, message, error);

        this.escribirLinea("[VERBOSE/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(this.fos);
            error.printStackTrace(ps);
            ps.flush();
        }
    }

    /**
     * Loguea un mensaje de WHAT A TERRIBLE FAILURE
     */
    public static void wtf(String tag, String message) {
        Logger.wtf(tag, message, null);
    }

    /**
     * Loguea un mensaje de WHAT A TERRIBLE FAILURE
     */
    public static void wtf(String tag, String message, Throwable error) {
        Log.wtf(tag, message, error);

        singleton.escribirLinea("[TERRIBLE/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(singleton.fos);
            error.printStackTrace(ps);
            ps.flush();
        }
    }

    /**
     * Loguea un mensaje de WHAT A TERRIBLE FAILURE, usando la instancia de <code>Logger</code> actual en vez del singleton
     */
    public void wtfU(String tag, String message) {
        this.wtfU(tag, message, null);
    }

    /**
     * Loguea un mensaje de WHAT A TERRIBLE FAILURE, usando la instancia de <code>Logger</code> actual en vez del singleton
     */
    public void wtfU(String tag, String message, Throwable error) {
        Log.wtf(tag, message, error);

        this.escribirLinea("[TERRIBLE/"+tag+"] "+message);
        if (error != null) {
            PrintStream ps = new PrintStream(this.fos);
            error.printStackTrace(ps);
            ps.flush();
        }
    }

}
