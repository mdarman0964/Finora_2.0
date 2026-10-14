package com.finora.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.R
import com.finora.data.local.BookEntity
import com.finora.data.local.CashEntryEntity
import com.finora.data.local.MemberEntity
import com.finora.domain.time.dateLabel
import com.finora.domain.time.monthBounds
import com.finora.ui.FinoraViewModel
import com.finora.ui.components.AmountText
import com.finora.ui.components.BalanceCard
import com.finora.ui.components.FinoraCard
import com.finora.ui.components.StatChip
import com.finora.ui.components.TrendChart
import com.finora.ui.components.categoryDisplayName
import com.finora.ui.components.formatAmount
import com.finora.ui.theme.CashIn
import com.finora.ui.theme.CashOut
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar

private enum class EntryFilter { ALL, IN, OUT }

@Composable
fun HomeScreen(viewModel: FinoraViewModel, selectedBookId: Int, onBookSelected: (Int) -> Unit) {
    val books by viewModel.books.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val banglaDigits by viewModel.banglaNumerals.collectAsStateWithLifecycle()
    var showAddBook by remember { mutableStateOf(false) }
    var showEntryEditor by remember { mutableStateOf(false) }
    var editedEntry by remember { mutableStateOf<CashEntryEntity?>(null) }

    LaunchedEffect(books) {
        if (books.isNotEmpty() && books.none { it.id == selectedBookId }) onBookSelected(books.first().id)
    }
    val book = books.firstOrNull { it.id == selectedBookId }
    val bookEntries = entries.filter { it.bookId == selectedBookId }
    val balance = (book?.openingBalance ?: 0L) + bookEntries.sumOf { if (it.type == "IN") it.amount else -it.amount }
    val now = System.currentTimeMillis()
    val range = monthBounds(now)
    val thisMonth = bookEntries.filter { it.date in range }
    val cashIn = thisMonth.filter { it.type == "IN" }.sumOf { it.amount }
    val cashOut = thisMonth.filter { it.type == "OUT" }.sumOf { it.amount }
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(EntryFilter.ALL) }
    var hideBalance by rememberSaveable { mutableStateOf(false) }
    var booksMenu by remember { mutableStateOf(false) }
    val view = LocalView.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.home_eyebrow), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineMedium)
                }
                Card(shape = CircleShape, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text("F", modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedButton(
                    onClick = { booksMenu = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(book?.name ?: stringResource(R.string.choose_book), maxLines = 1)
                    Icon(Icons.Outlined.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp).padding(start = 2.dp))
                }
                DropdownMenu(expanded = booksMenu, onDismissRequest = { booksMenu = false }) {
                    books.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = { onBookSelected(option.id); booksMenu = false }
                        )
                    }
                }
                IconButton(
                    onClick = { showAddBook = true },
                    modifier = Modifier.size(48.dp),
                    colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_book))
                }
            }
        }
        item {
            BalanceCard(
                label = stringResource(R.string.total_balance),
                amount = balance,
                hidden = hideBalance,
                banglaDigits = banglaDigits,
                onToggleVisibility = { hideBalance = !hideBalance }
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                StatChip(stringResource(R.string.cash_in), formatAmount(cashIn, banglaDigits), CashIn, Modifier.weight(1f))
                StatChip(stringResource(R.string.cash_out), formatAmount(cashOut, banglaDigits), CashOut, Modifier.weight(1f))
            }
        }
        item {
            FinoraCard {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.monthly_trend), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.last_seven_days), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TrendChart(lastSevenDays(bookEntries))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.recent_activity), style = MaterialTheme.typography.titleLarge)
                Text("${bookEntries.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.search_entries)) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filter == EntryFilter.ALL, onClick = { filter = EntryFilter.ALL }, label = { Text(stringResource(R.string.filter_all)) })
                FilterChip(selected = filter == EntryFilter.IN, onClick = { filter = EntryFilter.IN }, label = { Text(stringResource(R.string.cash_in)) })
                FilterChip(selected = filter == EntryFilter.OUT, onClick = { filter = EntryFilter.OUT }, label = { Text(stringResource(R.string.cash_out)) })
            }
        }
        val visibleEntries = bookEntries.filter { entry ->
            val matchesType = filter == EntryFilter.ALL || (filter == EntryFilter.IN && entry.type == "IN") || (filter == EntryFilter.OUT && entry.type == "OUT")
            val query = search.trim()
            matchesType && (query.isBlank() || entry.note.contains(query, true) || entry.category.contains(query, true))
        }
        if (visibleEntries.isEmpty()) {
            item {
                FinoraCard {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(if (search.isBlank()) R.string.empty_entries_title else R.string.no_results), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(if (search.isBlank()) R.string.empty_entries_body else R.string.try_another_search), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (search.isBlank()) {
                            Button(onClick = { showEntryEditor = true }) { Text(stringResource(R.string.add_first_entry)) }
                        }
                    }
                }
            }
        } else {
            items(visibleEntries, key = { it.id }) { entry ->
                EntryRow(entry, banglaDigits) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    editedEntry = entry
                    showEntryEditor = true
                }
            }
        }
    }

    if (showAddBook) {
        AddBookDialog(onDismiss = { showAddBook = false }, onSave = { name -> viewModel.addBook(name); showAddBook = false })
    }
    if (showEntryEditor && book != null) {
        EntryEditorSheet(
            viewModel = viewModel,
            bookId = editedEntry?.bookId ?: book.id,
            members = members.filter { it.isActive },
            initialEntry = editedEntry,
            onDismiss = { showEntryEditor = false; editedEntry = null }
        )
    }
}

@Composable
private fun EntryRow(entry: CashEntryEntity, banglaDigits: Boolean, onClick: () -> Unit) {
    FinoraCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = (if (entry.type == "IN") CashIn else CashOut).copy(alpha = 0.13f))
            ) {
                Icon(
                    imageVector = if (entry.type == "IN") Icons.Outlined.ArrowDownward else Icons.Outlined.ArrowUpward,
                    contentDescription = null,
                    tint = if (entry.type == "IN") CashIn else CashOut,
                    modifier = Modifier.padding(11.dp).size(18.dp)
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(categoryDisplayName(entry.category), style = MaterialTheme.typography.titleMedium)
                Text(
                    listOf(entry.note.takeIf { it.isNotBlank() }, dateLabel(entry.date)).filterNotNull().joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
            AmountText(entry.amount, entry.type == "IN", banglaDigits)
        }
    }
}

@Composable
private fun AddBookDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_book)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.book_name)) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name) }, enabled = name.isNotBlank()) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

private fun lastSevenDays(entries: List<CashEntryEntity>): List<Float> {
    val today = Calendar.getInstance()
    return (6 downTo 0).map { offset ->
        val day = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }
        val start = (day.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = (day.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        entries.filter { it.date in start..end }.sumOf { if (it.type == "IN") it.amount else -it.amount }.toFloat()
    }
}

