package com.finora.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE cash_entries ADD COLUMN section TEXT NOT NULL DEFAULT 'Personal'")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `notebook_entries` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `content` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `amount` INTEGER,
                    `date` INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinoraDatabase =
        Room.databaseBuilder(context, FinoraDatabase::class.java, "finora.db")
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDao(database: FinoraDatabase): FinoraDao = database.finoraDao()
}
