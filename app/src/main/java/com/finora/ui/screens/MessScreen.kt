package com.finora.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finora.R
import com.finora.data.local.MemberEntity
import com.finora.domain.mess.MemberMealInput
import com.finora.domain.mess.MemberSettlement
import com.finora.domain.mess.MessCalculator
import com.finora.domain.time.dateLabel
import com.finora.domain.time.monthBounds
import com.finora.domain.time.monthLabel
import com.finora.ui.FinoraViewModel
import com.finora.ui.components.FinoraCard
import com.finora.ui.components.MemberBarChart
import com.finora.ui.components.StatChip
import com.finora.ui.components.formatAmount
import com.finora.ui.theme.CashIn
import com.finora.ui.theme.CashOut
import java.util.Calendar

enum class MessForm { MEMBER, DEPOSIT, MEAL, BAZAR, PERSONAL }

@Composable
fun MessScreen(viewModel: FinoraViewModel) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val deposits by viewModel.deposits.collectAsStateWithLifecycle()
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    val bazar by viewModel.bazar.collectAsStateWithLifecycle()
    val personal by viewModel.personalExpenses.collectAsStateWithLifecycle()
    val banglaDigits by viewModel.banglaNumerals.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(0) }
    var monthOffset by rememberSaveable { mutableStateOf(0) }
    var actionMenu by remember { mutableStateOf(false) }
    var form by remember { mutableStateOf<MessForm?>(null) }
    var editedMember by remember { mutableStateOf<MemberEntity?>(null) }
    var deactivateTarget by remember { mutableStateOf<MemberEntity?>(null) }

    val monthTime = (Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }).timeInMillis
    val bounds = monthBounds(monthTime)
    val monthDeposits = deposits.filter { it.date in bounds }
    val monthMeals = meals.filter { it.date in bounds }
    val monthBazar = bazar.filter { it.date in bounds }
    val monthPersonal = personal.filter { it.date in bounds }
    val totalBazar = monthBazar.sumOf { it.amount }
    val totalMeals = monthMeals.sumOf { it.totalMeals }
    val rate = MessCalculator.mealRate(totalBazar, totalMeals)
    val includedMembers = members.filter { member ->
        member.isActive ||
            monthDeposits.any { it.memberId == member.id } ||
            monthBazar.any { it.memberId == member.id } ||
            monthMeals.any { it.memberId == member.id }
    }
    val settlements = MessCalculator.settle(
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(stringResource(R.string.mess_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.mess_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background) {
                listOf(R.string.summary, R.string.members, R.string.activity).forEachIndexed { index, label ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(stringResource(label)) })
                }
            }
        }
        when (tab) {
            0 -> {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatChip(stringResource(R.string.bazar_total), formatAmount(totalBazar, banglaDigits), CashOut, Modifier.weight(1f))
                        StatChip(stringResource(R.string.meal_rate), formatAmount((rate * 100).toLong(), banglaDigits), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    }
                }
                item {
                    FinoraCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.month_meals), style = MaterialTheme.typography.titleMedium)
                            Text("${totalMeals.toCleanNumber()}", style = MaterialTheme.typography.headlineMedium)
                            Text(
                                stringResource(R.string.calculation_formula),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                if (settlements.isNotEmpty()) {
                    item {
                        FinoraCard {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(stringResource(R.string.member_meals_chart), style = MaterialTheme.typography.titleMedium)
                                MemberBarChart(settlements.map { it.meals.toFloat() })
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    settlements.take(4).forEach { Text(it.memberName.take(7), style = MaterialTheme.typography.bodyMedium) }
                                }
                            }
                        }
                    }
                    item { Text(stringResource(R.string.who_gets_owes), style = MaterialTheme.typography.titleLarge) }
                    items(settlements, key = { "settlement-${it.memberId}" }) { SettlementRow(it, banglaDigits) }
                } else {
                    item { EmptyMessState(onAdd = { form = MessForm.MEMBER }) }
                }
                item {
                    FinoraCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.personal_expenses), style = MaterialTheme.typography.titleMedium)
                            Text(
                                formatAmount(monthPersonal.sumOf { it.amount }, banglaDigits),
                                style = MaterialTheme.typography.headlineMedium,
                                color = CashOut
                            )
                            Text(stringResource(R.string.personal_expenses_note), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            1 -> {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.active_members, members.count { it.isActive }), style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { editedMember = null; form = MessForm.MEMBER }) {
                            Icon(Icons.Outlined.PersonAdd, contentDescription = stringResource(R.string.add_member))
                        }
                    }
                }
                if (members.isEmpty()) {
                    item { EmptyMessState(onAdd = { form = MessForm.MEMBER }) }
                } else {
                    items(members, key = { "member-${it.id}" }) { member ->
                        MemberRow(
                            member = member,
                            settlements = settlements,
                            banglaDigits = banglaDigits,
                            onEdit = { editedMember = member; form = MessForm.MEMBER },
                            onToggle = { deactivateTarget = member }
                        )
                    }
                }
            }
            else -> {
                item { Text(stringResource(R.string.month_activity), style = MaterialTheme.typography.titleLarge) }
                val activity = buildList {
                    monthDeposits.forEach { row -> add(MessActivity(row.date, members.firstOrNull { it.id == row.memberId }?.name ?: "—", "deposit", row.amount, null)) }
                    monthBazar.forEach { row -> add(MessActivity(row.date, members.firstOrNull { it.id == row.memberId }?.name ?: "—", "bazar", row.amount, row.note)) }
                    monthPersonal.forEach { row -> add(MessActivity(row.date, members.firstOrNull { it.id == row.memberId }?.name ?: "—", "personal", row.amount, row.note)) }
                    monthMeals.forEach { row -> add(MessActivity(row.date, members.firstOrNull { it.id == row.memberId }?.name ?: "—", "meal", 0L, "${row.totalMeals.toCleanNumber()} meals")) }
                }.sortedByDescending { it.date }
                if (activity.isEmpty()) {
                    item { EmptyMessState(onAdd = { actionMenu = true }) }
                } else {
                    items(activity) { entry ->
                        FinoraCard {
                            Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(entry.member, style = MaterialTheme.typography.titleMedium)
                                    Text("${activityTitle(entry.kind)} · ${dateLabel(entry.date)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    entry.note?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                                if (entry.amount > 0) Text(formatAmount(entry.amount, banglaDigits), color = if (entry.kind == "deposit") CashIn else CashOut, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(6.dp))
            androidx.compose.material3.OutlinedButton(onClick = { actionMenu = true }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.add_mess_record), modifier = Modifier.padding(start = 8.dp))
            }
            DropdownMenu(expanded = actionMenu, onDismissRequest = { actionMenu = false }) {
                listOf(
                    MessForm.MEMBER to R.string.add_member,
                    MessForm.DEPOSIT to R.string.add_deposit,
                    MessForm.MEAL to R.string.log_meals,
                    MessForm.BAZAR to R.string.add_bazar,
                    MessForm.PERSONAL to R.string.add_personal_expense
                ).forEach { (kind, label) ->
                    DropdownMenuItem(
                        text = { Text(stringResource(label)) },
                        onClick = { actionMenu = false; editedMember = null; form = kind }
                    )
                }
            }
        }
    }

    if (form != null) {
        MessEntrySheet(
            viewModel = viewModel,
            kind = form!!,
            members = members.filter { it.isActive },
            editedMember = editedMember,
            onDismiss = { form = null; editedMember = null }
        )
    }
    deactivateTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deactivateTarget = null },
            title = { Text(stringResource(if (target.isActive) R.string.deactivate_member else R.string.activate_member)) },
            text = { Text(stringResource(if (target.isActive) R.string.deactivate_confirm else R.string.activate_confirm, target.name)) },
            confirmButton = {
                TextButton(onClick = { viewModel.toggleMember(target); deactivateTarget = null }) {
                    Text(stringResource(if (target.isActive) R.string.deactivate else R.string.activate))
                }
            },
            dismissButton = { TextButton(onClick = { deactivateTarget = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun SettlementRow(settlement: MemberSettlement, banglaDigits: Boolean) {
    val positive = settlement.balance >= 0
    FinoraCard {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(settlement.memberName, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.meals_and_share, settlement.meals.toCleanNumber(), formatAmount(settlement.mealCost, banglaDigits)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatAmount(kotlin.math.abs(settlement.balance), banglaDigits), color = if (positive) CashIn else CashOut, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(if (positive) R.string.gets_back else R.string.owes), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: MemberEntity,
    settlements: List<MemberSettlement>,
    banglaDigits: Boolean,
    onEdit: () -> Unit,
    onToggle: () -> Unit
) {
    val settlement = settlements.firstOrNull { it.memberId == member.id }
    FinoraCard {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(member.name.take(1).uppercase(), modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(member.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (member.isActive) settlement?.let { formatAmount(kotlin.math.abs(it.balance), banglaDigits) } ?: stringResource(R.string.no_activity_yet)
                    else stringResource(R.string.inactive),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit_member))
            }
            IconButton(onClick = onToggle, modifier = Modifier.size(44.dp)) {
                Icon(
                    Icons.Outlined.RemoveCircleOutline,
                    contentDescription = stringResource(if (member.isActive) R.string.deactivate else R.string.activate),
                    tint = if (member.isActive) MaterialTheme.colorScheme.error else CashIn
                )
            }
        }
    }
}

@Composable
private fun EmptyMessState(onAdd: () -> Unit) {
    FinoraCard {
        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.mess_empty_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.mess_empty_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onAdd) { Text(stringResource(R.string.add_member)) }
        }
    }
}

private data class MessActivity(val date: Long, val member: String, val kind: String, val amount: Long, val note: String?)

@Composable
private fun activityTitle(kind: String): String = when (kind) {
    "deposit" -> stringResource(R.string.deposit)
    "bazar" -> stringResource(R.string.bazar)
    "personal" -> stringResource(R.string.personal_expenses)
    else -> stringResource(R.string.meals)
}

private fun Double.toCleanNumber(): String = if (this % 1.0 == 0.0) toInt().toString() else String.format("%.1f", this)
