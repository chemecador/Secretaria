package com.chemecador.secretaria.provider;


import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.chemecador.secretaria.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;


/**
 * <code>FileProvider</code> asociado con los archivos internos de la aplicación<br/>
 *
 * Responde a <code>com.chemecador.secretaria.provider</code> y está configurado en <code>exported_files.xml</code>
 */
public class SecretariaFileProvider extends FileProvider {

    public SecretariaFileProvider() {
        super(R.xml.exported_files);
    }

    public static final String authority = "com.chemecador.secretaria.provider";

    /**
     * Alias de <code>getUriForFile(context, SecretariaFileProvider.authority, file)</code>
     * @param context   El contexto con el que conseguir el <code>Uri</code> del fichero
     * @param file      El <code>File</code> que apunta al fichero que se quiere abrir
     * @return          El <code>Uri</code> correspondiente para este Provider
     */
    public static Uri getUriForFile(@NotNull Context context, @NotNull File file) {
        return getUriForFile(context, authority, file);
    }

}
