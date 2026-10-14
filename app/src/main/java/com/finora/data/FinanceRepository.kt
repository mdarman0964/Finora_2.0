package com.finora.data

import com.finora.data.local.BazarEntity
import com.finora.data.local.BookEntity
import com.finora.data.local.CashEntryEntity
import com.finora.data.local.DepositEntity
import com.finora.data.local.FinoraDao
import com.finora.data.local.MealEntity
import com.finora.data.local.MemberEntity
import com.finora.data.local.PersonalExpenseEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class FinanceRepository @Inject constructor(private val dao: FinoraDao) {
    val books: Flow<List<BookEntity>> = dao.observeBooks()
    val entries: Flow<List<CashEntryEntity>> = dao.observeEntries()
    val members: Flow<List<MemberEntity>> = dao.observeMembers()
    val deposits: Flow<List<DepositEntity>> = dao.observeDeposits()
    val meals: Flow<List<MealEntity>> = dao.observeMeals()
    val bazar: Flow<List<BazarEntity>> = dao.observeBazar()
    val personalExpenses: Flow<List<PersonalExpenseEntity>> = dao.observePersonalExpenses()

    suspend fun ensureDefaultBook() {
        if (dao.bookCount() == 0) dao.saveBook(BookEntity(name = "Personal"))
    }

    suspend fun saveBook(name: String) = dao.saveBook(BookEntity(name = name.trim()))
    suspend fun saveEntry(entry: CashEntryEntity) = dao.saveEntry(entry)
    suspend fun deleteEntry(id: Int) = dao.deleteEntry(id)
    suspend fun saveMember(member: MemberEntity) = dao.saveMember(member)
    suspend fun updateMember(member: MemberEntity) = dao.updateMember(member)
    suspend fun saveDeposit(entry: DepositEntity) = dao.saveDeposit(entry)
    suspend fun saveMeal(entry: MealEntity) = dao.saveMeal(entry)
    suspend fun saveBazar(entry: BazarEntity) = dao.saveBazar(entry)
    suspend fun savePersonalExpense(entry: PersonalExpenseEntity) = dao.savePersonalExpense(entry)
    suspend fun getBook(id: Int) = dao.getBook(id)
}
