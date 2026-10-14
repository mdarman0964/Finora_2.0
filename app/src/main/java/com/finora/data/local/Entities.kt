package com.finora.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val openingBalance: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cash_entries")
data class CashEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: Int,
    val type: String,
    val amount: Long,
    val category: String,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val memberId: Int? = null
)

@Entity(tableName = "mess_members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mess_deposits")
data class DepositEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,
    val amount: Long,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "mess_meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,
    val date: Long = System.currentTimeMillis(),
    val breakfast: Double = 0.0,
    val lunch: Double = 0.0,
    val dinner: Double = 0.0,
    val guestMeals: Double = 0.0
) {
    val totalMeals: Double
        get() = breakfast + lunch + dinner + guestMeals
}

@Entity(tableName = "bazar_entries")
data class BazarEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,
    val amount: Long,
    val note: String = "",
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "personal_expenses")
data class PersonalExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,
    val amount: Long,
    val category: String = "Other",
    val note: String = "",
    val date: Long = System.currentTimeMillis()
)
