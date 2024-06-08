package com.chemecador.secretaria.core

import android.app.Application
import com.chemecador.secretaria.utils.log.FileLoggingTree
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        FileLoggingTree.deleteOldLogs(applicationContext)

        Timber.plant(FileLoggingTree(applicationContext))

        Timber.plant(Timber.DebugTree())

        configureFirestore()
    }

    private fun configureFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val cacheSettings = PersistentCacheSettings.newBuilder()
            .build()

        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(cacheSettings)
            .build()

        firestore.firestoreSettings = settings
    }
}

