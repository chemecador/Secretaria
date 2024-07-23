package com.chemecador.secretaria.di

import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.repositories.UserRepository
import com.chemecador.secretaria.data.repositories.friends.FriendsRepository
import com.chemecador.secretaria.data.repositories.friends.FriendsRepositoryImpl
import com.chemecador.secretaria.data.repositories.main.MainRepository
import com.chemecador.secretaria.data.repositories.main.MainRepositoryImpl
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
    fun provideMainRepository(
        firestore: FirebaseFirestore,
        userRepository: UserRepository,
        res: ResourceProvider
    ): MainRepository =
        MainRepositoryImpl(firestore, userRepository, res)

    @Provides
    @Singleton
    fun provideFriendsRepository(
        firestore: FirebaseFirestore,
        res: ResourceProvider
    ): FriendsRepository =
        FriendsRepositoryImpl(firestore, res)
}