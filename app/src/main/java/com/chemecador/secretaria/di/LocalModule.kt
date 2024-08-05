package com.chemecador.secretaria.di

import android.content.Context
import com.chemecador.secretaria.data.local.UserPreferences
import com.chemecador.secretaria.data.provider.ResourceProvider
import com.chemecador.secretaria.data.provider.ResourceProviderImpl
import com.chemecador.secretaria.data.repositories.UserRepository
import com.chemecador.secretaria.data.services.AuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideResourceProvider(@ApplicationContext context: Context): ResourceProvider =
        ResourceProviderImpl(context)


    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context) = UserPreferences(context)


    @Provides
    @Singleton
    fun provideUserRepository(userPreferences: UserPreferences, authService: AuthService) =
        UserRepository(userPreferences, authService)

}