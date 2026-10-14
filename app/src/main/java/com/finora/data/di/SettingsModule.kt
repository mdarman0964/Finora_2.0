package com.finora.data.di

import android.content.Context
import com.finora.data.prefs.UserSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {
    @Provides
    @Singleton
    fun provideUserSettings(@ApplicationContext context: Context): UserSettings = UserSettings(context)
}
