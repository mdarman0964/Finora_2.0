package com.finora.ui.screens

import android.content.ActivityNotFoundException
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.R
import com.finora.data.local.CashEntryEntity
import com.finora.domain.export.ReportExporter
import com.finora.domain.time.dateLabel
import com.finora.domain.time.monthBounds
import com.finora.domain.time.monthLabel
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
import java.util.Calendar

private enum class BusinessFilter { ALL, IN, OUT }

@Composable
fun BusinessScreen(
    viewModel: FinoraViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    val books by viewModel.books.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val banglaDigits by viewModel.banglaNumerals.collectAsStateWithLifecycle()

    var showEntryEditor by remember { mutableStateOf(false) }
    var editedEntry by remember { mutableStateOf<CashEntryEntity?>(null) }
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(BusinessFilter.ALL) }
    var hideBalance by rememberSaveable { mutableStateOf(false) }

    val businessEntries = entries.filter { it.section == "Business" }
    val businessBook = books.firstOrNull { it.name.equals("Business", ignoreCase = true) } ?: books.firstOrNull()
    val balance = businessEntries.sumOf { if (it.type == "IN") it.amount else -it.amount }

    val now = System.currentTimeMillis()
    val range = monthBounds(now)
    val thisMonth = businessEntries.filter { it.date in range }
    val revenue = thisMonth.filter { it.type == "IN" }.sumOf { it.amount }
    val expense = thisMonth.filter { it.type == "OUT" }.sumOf { it.amount }
    val profit = revenue - expense

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Outlined.Menu, contentDescription = stringResource(R.string.open_menu))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            stringResource(R.string.section_business),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(stringResource(R.string.business_title), style = MaterialTheme.typography.headlineSmall)
                    }
                }
                IconButton(
                    onClick = { showEntryEditor = true },
                    modifier = Modifier.size(44.dp),
                    colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_entry), tint = MaterialTheme.colorScheme.onPrimary)
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
                StatChip(stringResource(R.string.cash_in), formatAmount(revenue, banglaDigits), CashIn, Modifier.weight(1f))
                StatChip(stringResource(R.string.cash_out), formatAmount(expense, banglaDigits), CashOut, Modifier.weight(1f))
            }
        }

        item {
            FinoraCard {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.net_profit), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            formatAmount(profit, banglaDigits),
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (profit >= 0) CashIn else CashOut
                        )
                    }
                    Button(
                        onClick = {
                            try {
                                ReportExporter.shareBusinessPdf(context, now, thisMonth, revenue, expense)
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(context, R.string.share_unavailable, Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(stringResource(R.string.export_pdf), modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        }

        item {
            FinoraCard {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.monthly_trend), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.last_seven_days), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TrendChart(lastSevenDaysBusiness(businessEntries))
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.recent_activity), style = MaterialTheme.typography.titleLarge)
                Text("${businessEntries.size}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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
                FilterChip(selected = filter == BusinessFilter.ALL, onClick = { filter = BusinessFilter.ALL }, label = { Text(stringResource(R.string.filter_all)) })
                FilterChip(selected = filter == BusinessFilter.IN, onClick = { filter = BusinessFilter.IN }, label = { Text(stringResource(R.string.cash_in)) })
                FilterChip(selected = filter == BusinessFilter.OUT, onClick = { filter = BusinessFilter.OUT }, label = { Text(stringResource(R.string.cash_out)) })
            }
        }

        val visibleEntries = businessEntries.filter { entry ->
            val matchesType = filter == BusinessFilter.ALL || (filter == BusinessFilter.IN && entry.type == "IN") || (filter == BusinessFilter.OUT && entry.type == "OUT")
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
                BusinessEntryRow(entry, banglaDigits) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    editedEntry = entry
                    showEntryEditor = true
                }
            }
        }
    }

    if (showEntryEditor) {
        EntryEditorSheet(
            viewModel = viewModel,
            bookId = editedEntry?.bookId ?: businessBook?.id ?: 1,
            members = members.filter { it.isActive },
            initialEntry = editedEntry,
            defaultSection = "Business",
            onDismiss = { showEntryEditor = false; editedEntry = null }
        )
    }
}

@Composable
private fun BusinessEntryRow(entry: CashEntryEntity, banglaDigits: Boolean, onClick: () -> Unit) {
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

private fun lastSevenDaysBusiness(entries: List<CashEntryEntity>): List<Float> {
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
