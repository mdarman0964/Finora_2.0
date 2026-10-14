package com.finora.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FinoraDao {
    @Query("SELECT * FROM books ORDER BY createdAt ASC")
    fun observeBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBook(id: Int): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBook(book: BookEntity): Long

    @Query("SELECT COUNT(*) FROM books")
    suspend fun bookCount(): Int

    @Query("SELECT * FROM cash_entries ORDER BY date DESC, id DESC")
    fun observeEntries(): Flow<List<CashEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEntry(entry: CashEntryEntity)

    @Query("DELETE FROM cash_entries WHERE id = :id")
    suspend fun deleteEntry(id: Int)

    @Query("SELECT * FROM mess_members ORDER BY createdAt ASC")
    fun observeMembers(): Flow<List<MemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMember(member: MemberEntity): Long

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Query("SELECT * FROM mess_deposits ORDER BY date DESC")
    fun observeDeposits(): Flow<List<DepositEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDeposit(deposit: DepositEntity)

    @Query("SELECT * FROM mess_meals ORDER BY date DESC")
    fun observeMeals(): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMeal(meal: MealEntity)

    @Query("SELECT * FROM bazar_entries ORDER BY date DESC")
    fun observeBazar(): Flow<List<BazarEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBazar(entry: BazarEntity)

    @Query("SELECT * FROM personal_expenses ORDER BY date DESC")
    fun observePersonalExpenses(): Flow<List<PersonalExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePersonalExpense(entry: PersonalExpenseEntity)
}
