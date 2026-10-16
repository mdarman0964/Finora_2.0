package com.finora.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.data.FinanceRepository
import com.finora.data.local.BazarEntity
import com.finora.data.local.BookEntity
import com.finora.data.local.CashEntryEntity
import com.finora.data.local.DepositEntity
import com.finora.data.local.MealEntity
import com.finora.data.local.MemberEntity
import com.finora.data.local.PersonalExpenseEntity
import com.finora.data.local.NoteEntity
import com.finora.data.prefs.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FinoraViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val settings: UserSettings
) : ViewModel() {
    val books = repository.books.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val entries = repository.entries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val members = repository.members.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val deposits = repository.deposits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val meals = repository.meals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val bazar = repository.bazar.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val personalExpenses = repository.personalExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val notes = repository.notes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val themeChoice: StateFlow<String> = settings.themeChoice
    val language: StateFlow<String> = settings.language
    val banglaNumerals: StateFlow<Boolean> = settings.banglaNumerals

    init {
        viewModelScope.launch { repository.ensureDefaultBook() }
    }

    fun addBook(name: String) = viewModelScope.launch {
        if (name.isNotBlank()) repository.saveBook(name)
    }

    fun saveEntry(entry: CashEntryEntity) = viewModelScope.launch { repository.saveEntry(entry) }
    fun deleteEntry(id: Int) = viewModelScope.launch { repository.deleteEntry(id) }
    fun saveMember(member: MemberEntity) = viewModelScope.launch {
        if (member.name.isNotBlank()) {
            if (member.id == 0) repository.saveMember(member) else repository.updateMember(member)
        }
    }

    fun toggleMember(member: MemberEntity) = viewModelScope.launch {
        repository.updateMember(member.copy(isActive = !member.isActive))
    }

    fun saveDeposit(memberId: Int, amount: Long, date: Long) =
        viewModelScope.launch { repository.saveDeposit(DepositEntity(memberId = memberId, amount = amount, date = date)) }

    fun saveMeal(memberId: Int, date: Long, breakfast: Double, lunch: Double, dinner: Double, guest: Double) =
        viewModelScope.launch {
            repository.saveMeal(
                MealEntity(
                    memberId = memberId,
                    date = date,
                    breakfast = breakfast,
                    lunch = lunch,
                    dinner = dinner,
                    guestMeals = guest
                )
            )
        }

    fun saveBazar(memberId: Int, amount: Long, note: String, date: Long) =
        viewModelScope.launch { repository.saveBazar(BazarEntity(memberId = memberId, amount = amount, note = note, date = date)) }

    fun savePersonalExpense(memberId: Int, amount: Long, category: String, note: String, date: Long) =
        viewModelScope.launch {
            repository.savePersonalExpense(
                PersonalExpenseEntity(memberId = memberId, amount = amount, category = category, note = note, date = date)
            )
        }

    fun saveNote(note: NoteEntity) = viewModelScope.launch { repository.saveNote(note) }
    fun deleteNote(id: Int) = viewModelScope.launch { repository.deleteNote(id) }

    fun setThemeChoice(value: String) = settings.setThemeChoice(value)
    fun setLanguage(value: String) = settings.setLanguage(value)
    fun setBanglaNumerals(value: Boolean) = settings.setBanglaNumerals(value)

    fun book(id: Int): BookEntity? = books.value.firstOrNull { it.id == id }
}
