package com.finora.ui.screens

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.R
import com.finora.data.local.CashEntryEntity
import com.finora.domain.export.ReportExporter
import com.finora.domain.export.MessReportRecord
import com.finora.domain.mess.MemberMealInput
import com.finora.domain.mess.MemberSettlement
import com.finora.domain.mess.MessCalculator
import com.finora.domain.time.monthBounds
import com.finora.domain.time.monthLabel
import com.finora.ui.FinoraViewModel
import com.finora.ui.components.DonutChart
import com.finora.ui.components.FinoraCard
import com.finora.ui.components.StatChip
import com.finora.ui.components.formatAmount
import com.finora.ui.theme.CashIn
import com.finora.ui.theme.CashOut
import com.finora.ui.theme.Cyan
import com.finora.ui.theme.Violet
import java.util.Calendar

@Composable
fun ReportsScreen(viewModel: FinoraViewModel) {
    val context = LocalContext.current
    val books by viewModel.books.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val deposits by viewModel.deposits.collectAsStateWithLifecycle()
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    val bazar by viewModel.bazar.collectAsStateWithLifecycle()
    val personal by viewModel.personalExpenses.collectAsStateWithLifecycle()
    val banglaDigits by viewModel.banglaNumerals.collectAsStateWithLifecycle()
    var selectedBookId by rememberSaveable { mutableStateOf(0) }
    var monthOffset by rememberSaveable { mutableStateOf(0) }
    var bookMenu by remember { mutableStateOf(false) }
    var exportError by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(books) {
        if (books.isNotEmpty() && books.none { it.id == selectedBookId }) selectedBookId = books.first().id
    }
    val book = books.firstOrNull { it.id == selectedBookId }
    val monthTime = Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }.timeInMillis
    val bounds = monthBounds(monthTime)
    val monthEntries = entries.filter { it.bookId == selectedBookId && it.date in bounds }
    val income = monthEntries.filter { it.type == "IN" }.sumOf { it.amount }
    val expenses = monthEntries.filter { it.type == "OUT" }.sumOf { it.amount }
    val categoryTotals = monthEntries.filter { it.type == "OUT" }.groupBy { it.category }
        .mapValues { (_, rows) -> rows.sumOf { it.amount }.toFloat() }
        .toList().sortedByDescending { it.second }.take(5)
    val totalBazar = bazar.filter { it.date in bounds }.sumOf { it.amount }
    val monthBazar = bazar.filter { it.date in bounds }
    val monthPersonal = personal.filter { it.date in bounds }
    val personalTotal = monthPersonal.sumOf { it.amount }
    val monthMeals = meals.filter { it.date in bounds }
    val monthDeposits = deposits.filter { it.date in bounds }
    val includedMembers = members.filter { member ->
        member.isActive ||
            monthDeposits.any { it.memberId == member.id } ||
            monthBazar.any { it.memberId == member.id } ||
            monthMeals.any { it.memberId == member.id }
    }
    val settlements: List<MemberSettlement> = MessCalculator.settle(
        includedMembers.map { member ->
            MemberMealInput(
                memberId = member.id,
                memberName = member.name,
                deposits = monthDeposits.filter { it.memberId == member.id }.sumOf { it.amount },
                bazarSpending = monthBazar.filter { it.memberId == member.id }.sumOf { it.amount },
                meals = monthMeals.filter { it.memberId == member.id }.sumOf { it.totalMeals }
            )
        },
        totalBazar
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Text(stringResource(R.string.reports_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.reports_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { bookMenu = true }, modifier = Modifier.weight(1f)) {
                    Text(book?.name ?: stringResource(R.string.choose_book), maxLines = 1)
                }
                androidx.compose.material3.DropdownMenu(expanded = bookMenu, onDismissRequest = { bookMenu = false }) {
                    books.forEach { option ->
                        androidx.compose.material3.DropdownMenuItem(text = { Text(option.name) }, onClick = { selectedBookId = option.id; bookMenu = false })
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                StatChip(stringResource(R.string.bazar_total), formatAmount(totalBazar, banglaDigits), Violet, Modifier.weight(1f))
                StatChip(stringResource(R.string.personal_expenses), formatAmount(personalTotal, banglaDigits), CashOut, Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { monthOffset -= 1 }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = stringResource(R.string.previous_month))
                }
                Text(monthLabel(monthTime), style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { monthOffset += 1 }, modifier = Modifier.size(44.dp), enabled = monthOffset < 0) {
                    Icon(Icons.Outlined.ArrowForwardIos, contentDescription = stringResource(R.string.next_month))
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                StatChip(stringResource(R.string.cash_in), formatAmount(income, banglaDigits), CashIn, Modifier.weight(1f))
                StatChip(stringResource(R.string.cash_out), formatAmount(expenses, banglaDigits), CashOut, Modifier.weight(1f))
            }
        }
        item {
            FinoraCard {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(stringResource(R.string.net_movement), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatAmount(income - expenses, banglaDigits), style = MaterialTheme.typography.headlineMedium, color = if (income >= expenses) CashIn else CashOut)
                    Text(stringResource(R.string.monthly_entry_count, monthEntries.size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            val categoryColors = listOf(Cyan, Violet, CashIn, CashOut, MaterialTheme.colorScheme.primary)
            FinoraCard {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    DonutChart(
                        values = categoryTotals.map { it.second },
                        colors = categoryColors,
                        centerLabel = stringResource(R.string.categories)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.expense_categories), style = MaterialTheme.typography.titleMedium)
                        if (categoryTotals.isEmpty()) {
                            Text(stringResource(R.string.no_category_data), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            categoryTotals.forEachIndexed { index, (name, amount) ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    androidx.compose.foundation.Canvas(Modifier.size(8.dp)) { drawCircle(categoryColors[index]) }
                                    Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                    Text(formatAmount(amount.toLong(), banglaDigits), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Text(stringResource(R.string.member_statement), style = MaterialTheme.typography.titleLarge) }
        if (settlements.isEmpty()) {
            item {
                FinoraCard {
                    Text(stringResource(R.string.no_member_statement), modifier = Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(settlements, key = { "report-${it.memberId}" }) { row ->
                FinoraCard {
                    Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(row.memberName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.statement_detail, row.meals.toString(), formatAmount(row.deposits, banglaDigits), formatAmount(row.bazarSpending, banglaDigits)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatAmount(kotlin.math.abs(row.balance), banglaDigits), color = if (row.balance >= 0) CashIn else CashOut, style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(if (row.balance >= 0) R.string.gets_back else R.string.owes), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        if (book != null) try {
                            val memberName = members.associate { it.id to it.name }
                            val messRecords = buildList {
                                monthDeposits.forEach { row ->
                                    add(MessReportRecord(row.date, memberName[row.memberId] ?: "—", "Deposit", row.amount))
                                }
                                monthBazar.forEach { row ->
                                    add(MessReportRecord(row.date, memberName[row.memberId] ?: "—", "Shared bazar", row.amount, note = row.note))
                                }
                                monthMeals.forEach { row ->
                                    add(MessReportRecord(row.date, memberName[row.memberId] ?: "—", "Meals", units = row.totalMeals))
                                }
                                monthPersonal.forEach { row ->
                                    add(MessReportRecord(row.date, memberName[row.memberId] ?: "—", "Personal expense · ${row.category}", row.amount, note = row.note))
                                }
                            }.sortedByDescending { it.date }
                            ReportExporter.shareCsv(context, book, monthTime, monthEntries, settlements, messRecords)
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(context, R.string.share_unavailable, Toast.LENGTH_SHORT).show()
                        } catch (_: Exception) {
                            exportError = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.export_csv), modifier = Modifier.padding(start = 6.dp))
                }
                Button(
                    onClick = {
                        if (book != null) try {
                            ReportExporter.sharePdf(context, book, monthTime, income, expenses, settlements)
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(context, R.string.share_unavailable, Toast.LENGTH_SHORT).show()
                        } catch (_: Exception) {
                            exportError = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.export_pdf), modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }

    if (exportError) {
        AlertDialog(
            onDismissRequest = { exportError = false },
            title = { Text(stringResource(R.string.export_error_title)) },
            text = { Text(stringResource(R.string.export_error_body)) },
            confirmButton = { TextButton(onClick = { exportError = false }) { Text(stringResource(R.string.done)) } }
        )
    }
}
