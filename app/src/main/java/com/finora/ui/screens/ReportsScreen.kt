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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.Composable
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
import com.finora.domain.export.MessReportRecord
import com.finora.domain.export.ReportExporter
import com.finora.domain.mess.MemberMealInput
import com.finora.domain.mess.MemberSettlement
import com.finora.domain.mess.MessCalculator
import com.finora.domain.time.dateLabel
import com.finora.domain.time.monthBounds
import com.finora.domain.time.monthLabel
import com.finora.ui.FinoraViewModel
import com.finora.ui.components.DonutChart
import com.finora.ui.components.FinoraCard
import com.finora.ui.components.StatChip
import com.finora.ui.components.categoryDisplayName
import com.finora.ui.components.formatAmount
import com.finora.ui.theme.CashIn
import com.finora.ui.theme.CashOut
import com.finora.ui.theme.Cyan
import com.finora.ui.theme.Violet
import java.util.Calendar

enum class ReportSection { PERSONAL, BUSINESS, BACHELOR, NOTEBOOK }

@Composable
fun ReportsScreen(
    viewModel: FinoraViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current
    val books by viewModel.books.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val deposits by viewModel.deposits.collectAsStateWithLifecycle()
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    val bazar by viewModel.bazar.collectAsStateWithLifecycle()
    val personal by viewModel.personalExpenses.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val banglaDigits by viewModel.banglaNumerals.collectAsStateWithLifecycle()

    var activeSection by rememberSaveable { mutableStateOf(ReportSection.PERSONAL) }
    var monthOffset by rememberSaveable { mutableStateOf(0) }
    var exportError by remember { mutableStateOf(false) }

    val monthTime = Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }.timeInMillis
    val bounds = monthBounds(monthTime)

    // Data partitioning by section
    val personalEntries = entries.filter { (it.section == "Personal" || it.section.isBlank()) && it.date in bounds }
    val personalIncome = personalEntries.filter { it.type == "IN" }.sumOf { it.amount }
    val personalExpenses = personalEntries.filter { it.type == "OUT" }.sumOf { it.amount }

    val businessEntries = entries.filter { it.section == "Business" && it.date in bounds }
    val businessRevenue = businessEntries.filter { it.type == "IN" }.sumOf { it.amount }
    val businessExpense = businessEntries.filter { it.type == "OUT" }.sumOf { it.amount }

    val monthBazar = bazar.filter { it.date in bounds }
    val totalBazar = monthBazar.sumOf { it.amount }
    val monthMeals = meals.filter { it.date in bounds }
    val totalMeals = monthMeals.sumOf { it.totalMeals }
    val monthDeposits = deposits.filter { it.date in bounds }
    val mealRate = MessCalculator.mealRate(totalBazar, totalMeals)

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

    val monthNotes = notes.filter { it.date in bounds }

    var showEntryEditor by rememberSaveable { mutableStateOf(false) }
    var entrySection by rememberSaveable { mutableStateOf("Personal") }
    var showMessDeposit by rememberSaveable { mutableStateOf(false) }
    var showNoteEditor by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (activeSection) {
                        ReportSection.BACHELOR -> showMessDeposit = true
                        ReportSection.NOTEBOOK -> showNoteEditor = true
                        ReportSection.BUSINESS -> {
                            entrySection = "Business"
                            showEntryEditor = true
                        }
                        ReportSection.PERSONAL -> {
                            entrySection = "Personal"
                            showEntryEditor = true
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("reports_fab_add_entry")
            ) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_entry))
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, top = 22.dp, end = 18.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onOpenDrawer, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Outlined.Menu, contentDescription = stringResource(R.string.open_menu))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.reports_title), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(R.string.reports_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Section Selector Tabs
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeSection == ReportSection.PERSONAL,
                    onClick = { activeSection = ReportSection.PERSONAL },
                    label = { Text(stringResource(R.string.section_personal)) }
                )
                FilterChip(
                    selected = activeSection == ReportSection.BUSINESS,
                    onClick = { activeSection = ReportSection.BUSINESS },
                    label = { Text(stringResource(R.string.section_business)) }
                )
                FilterChip(
                    selected = activeSection == ReportSection.BACHELOR,
                    onClick = { activeSection = ReportSection.BACHELOR },
                    label = { Text(stringResource(R.string.section_bachelor)) }
                )
                FilterChip(
                    selected = activeSection == ReportSection.NOTEBOOK,
                    onClick = { activeSection = ReportSection.NOTEBOOK },
                    label = { Text(stringResource(R.string.section_notebook)) }
                )
            }
        }

        // Month Selector
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

        // Section-specific display
        when (activeSection) {
            ReportSection.PERSONAL -> {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                        StatChip(stringResource(R.string.cash_in), formatAmount(personalIncome, banglaDigits), CashIn, Modifier.weight(1f))
                        StatChip(stringResource(R.string.cash_out), formatAmount(personalExpenses, banglaDigits), CashOut, Modifier.weight(1f))
                    }
                }
                item {
                    FinoraCard {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(stringResource(R.string.net_movement), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                formatAmount(personalIncome - personalExpenses, banglaDigits),
                                style = MaterialTheme.typography.headlineMedium,
                                color = if (personalIncome >= personalExpenses) CashIn else CashOut
                            )
                            val pCount = if (banglaDigits) com.finora.domain.time.toBanglaDigits("${personalEntries.size}") else "${personalEntries.size}"
                            Text(stringResource(R.string.monthly_entry_count, pCount), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    val categoryTotals = personalEntries.filter { it.type == "OUT" }.groupBy { it.category }
                        .mapValues { (_, rows) -> rows.sumOf { it.amount }.toFloat() }
                        .toList().sortedByDescending { it.second }.take(5)
                    CategoryDonutCard(categoryTotals, banglaDigits)
                }
            }

            ReportSection.BUSINESS -> {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                        StatChip(stringResource(R.string.cash_in), formatAmount(businessRevenue, banglaDigits), CashIn, Modifier.weight(1f))
                        StatChip(stringResource(R.string.cash_out), formatAmount(businessExpense, banglaDigits), CashOut, Modifier.weight(1f))
                    }
                }
                item {
                    FinoraCard {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(stringResource(R.string.net_profit), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                formatAmount(businessRevenue - businessExpense, banglaDigits),
                                style = MaterialTheme.typography.headlineMedium,
                                color = if (businessRevenue >= businessExpense) CashIn else CashOut
                            )
                            val bCount = if (banglaDigits) com.finora.domain.time.toBanglaDigits("${businessEntries.size}") else "${businessEntries.size}"
                            Text(stringResource(R.string.monthly_business_count, bCount), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item {
                    val categoryTotals = businessEntries.filter { it.type == "OUT" }.groupBy { it.category }
                        .mapValues { (_, rows) -> rows.sumOf { it.amount }.toFloat() }
                        .toList().sortedByDescending { it.second }.take(5)
                    CategoryDonutCard(categoryTotals, banglaDigits)
                }
            }

            ReportSection.BACHELOR -> {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                        StatChip(stringResource(R.string.bazar_total), formatAmount(totalBazar, banglaDigits), Violet, Modifier.weight(1f))
                        StatChip(stringResource(R.string.meal_rate), formatAmount((mealRate * 100).toLong(), banglaDigits), Cyan, Modifier.weight(1f))
                    }
                }
                item {
                    FinoraCard {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(stringResource(R.string.month_meals), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val mealsFormatted = if (banglaDigits) com.finora.domain.time.toBanglaDigits("%.1f".format(totalMeals)) else "%.1f".format(totalMeals)
                            Text(mealsFormatted, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            val memberCount = if (banglaDigits) com.finora.domain.time.toBanglaDigits("${includedMembers.size}") else "${includedMembers.size}"
                            Text(stringResource(R.string.participating_members_count, memberCount), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    val rowMeals = if (banglaDigits) com.finora.domain.time.toBanglaDigits(row.meals.toString()) else row.meals.toString()
                                    Text(
                                        stringResource(R.string.statement_detail, rowMeals, formatAmount(row.deposits, banglaDigits), formatAmount(row.bazarSpending, banglaDigits)),
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
            }

            ReportSection.NOTEBOOK -> {
                val totalNoteAmounts = monthNotes.mapNotNull { it.amount }.sum()
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
                        val countStr = if (banglaDigits || java.util.Locale.getDefault().language == "bn") com.finora.domain.time.toBanglaDigits("${monthNotes.size}") else "${monthNotes.size}"
                        StatChip(stringResource(R.string.total_notes_stat), countStr, Cyan, Modifier.weight(1f))
                        StatChip(stringResource(R.string.tracked_dues_stat), formatAmount(totalNoteAmounts, banglaDigits), Violet, Modifier.weight(1f))
                    }
                }
                if (monthNotes.isEmpty()) {
                    item {
                        FinoraCard {
                            Text(stringResource(R.string.empty_notes_title), modifier = Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(monthNotes, key = { "note-${it.id}" }) { note ->
                        FinoraCard {
                            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(note.title, style = MaterialTheme.typography.titleMedium)
                                    if (note.amount != null && note.amount > 0) {
                                        Text(formatAmount(note.amount, banglaDigits), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                                    }
                                }
                                Text(note.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(categoryDisplayName(note.category), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(dateLabel(note.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Export PDF and CSV Buttons
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        try {
                            when (activeSection) {
                                ReportSection.PERSONAL -> {
                                    ReportExporter.sharePersonalPdf(context, monthTime, personalEntries, personalIncome, personalExpenses)
                                }
                                ReportSection.BUSINESS -> {
                                    ReportExporter.shareBusinessPdf(context, monthTime, businessEntries, businessRevenue, businessExpense)
                                }
                                ReportSection.BACHELOR -> {
                                    ReportExporter.shareBachelorPdf(context, monthTime, settlements, totalBazar, totalMeals, mealRate)
                                }
                                ReportSection.NOTEBOOK -> {
                                    ReportExporter.shareNotebookPdf(context, monthTime, monthNotes)
                                }
                            }
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

                Button(
                    onClick = {
                        try {
                            val defaultBook = books.firstOrNull() ?: com.finora.data.local.BookEntity(name = activeSection.name)
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
                            }.sortedByDescending { it.date }
                            val activeEntries = when (activeSection) {
                                ReportSection.PERSONAL -> personalEntries
                                ReportSection.BUSINESS -> businessEntries
                                else -> personalEntries + businessEntries
                            }
                            ReportExporter.shareCsv(context, defaultBook, monthTime, activeEntries, settlements, messRecords)
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
            }
        }
    }
    }

    if (showEntryEditor) {
        val bookId = books.firstOrNull()?.id ?: 0
        EntryEditorSheet(
            viewModel = viewModel,
            bookId = bookId,
            members = members.filter { it.isActive },
            initialEntry = null,
            defaultSection = entrySection,
            onDismiss = { showEntryEditor = false }
        )
    }

    if (showMessDeposit) {
        MessEntrySheet(
            viewModel = viewModel,
            kind = MessForm.DEPOSIT,
            members = members.filter { it.isActive },
            editedMember = null,
            initialMemberId = null,
            onDismiss = { showMessDeposit = false }
        )
    }

    if (showNoteEditor) {
        NoteEditorDialog(
            note = null,
            onDismiss = { showNoteEditor = false },
            onSave = { updated ->
                viewModel.saveNote(updated)
                showNoteEditor = false
            },
            onDelete = { showNoteEditor = false }
        )
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

@Composable
private fun CategoryDonutCard(categoryTotals: List<Pair<String, Float>>, banglaDigits: Boolean) {
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
                            androidx.compose.foundation.Canvas(Modifier.size(8.dp)) { drawCircle(categoryColors[index % categoryColors.size]) }
                            Text(categoryDisplayName(name), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            Text(formatAmount(amount.toLong(), banglaDigits), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
