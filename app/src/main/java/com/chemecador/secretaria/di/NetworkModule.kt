package com.chemecador.secretaria.di

import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.repositories.FirestoreRepository
import com.chemecador.secretaria.data.repositories.OnlineRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideRepository(firestore: FirebaseFirestore, res: ResourceProvider): OnlineRepository =
        FirestoreRepository(firestore, res)
}