package com.finora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookEntity::class,
        CashEntryEntity::class,
        MemberEntity::class,
        DepositEntity::class,
        MealEntity::class,
        BazarEntity::class,
        PersonalExpenseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinoraDatabase : RoomDatabase() {
    abstract fun finoraDao(): FinoraDao
}
