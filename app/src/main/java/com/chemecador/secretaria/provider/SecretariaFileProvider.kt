package com.chemecador.secretaria.provider

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * `FileProvider` asociado con los archivos internos de la aplicación<br></br>
 *
 * Responde a `com.chemecador.secretaria.provider` y está configurado en `exported_files.xml`
 */
object SecretariaFileProvider : FileProvider() {
    const val authority = "com.chemecador.secretaria.provider"

    /**
     * Alias de `getUriForFile(context, SecretariaFileProvider.authority, file)`
     * @param context   El contexto con el que conseguir el `Uri` del fichero
     * @param file      El `File` que apunta al fichero que se quiere abrir
     * @return          El `Uri` correspondiente para este Provider
     */
    fun getUriForFile(context: Context, file: File): Uri {
        return getUriForFile(context, authority, file)
    }
}