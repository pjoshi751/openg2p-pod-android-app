package com.openg2p.pod.di

import android.content.Context
import com.openg2p.pod.data.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Install in the ApplicationComponent
object AppModule {

    @Provides
    @Singleton // Ensure only one instance of SettingsRepository exists
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        // SettingsRepository constructor requires Context
        return SettingsRepository(context)
    }

    // Provide Application Context if needed elsewhere
    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }
}
