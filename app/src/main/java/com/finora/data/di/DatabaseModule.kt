package com.finora.data.di

import android.content.Context
import androidx.room.Room
import com.finora.data.local.FinoraDao
import com.finora.data.local.FinoraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinoraDatabase =
        Room.databaseBuilder(context, FinoraDatabase::class.java, "finora.db").build()

    @Provides
    fun provideDao(database: FinoraDatabase): FinoraDao = database.finoraDao()
}
