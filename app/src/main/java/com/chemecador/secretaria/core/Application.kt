package com.chemecador.secretaria.core

import android.app.Application
import com.chemecador.secretaria.utils.log.FileLoggingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        // Se crean las instancias necesarias antes de cargar la aplicación
        FileLoggingTree.deleteOldLogs(applicationContext)

        Timber.plant(FileLoggingTree(applicationContext))
    }
}

