package com.finora.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingCart
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import java.util.Locale
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
fun MessScreen(
    viewModel: FinoraViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val deposits by viewModel.deposits.collectAsStateWithLifecycle()
    val meals by viewModel.meals.collectAsStateWithLifecycle()
    val bazar by viewModel.bazar.collectAsStateWithLifecycle()
    val personal by viewModel.personalExpenses.collectAsStateWithLifecycle()
    val banglaDigits by viewModel.banglaNumerals.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var tab by rememberSaveable { mutableStateOf(0) }
    var monthOffset by rememberSaveable { mutableStateOf(0) }
    var actionMenu by remember { mutableStateOf(false) }
    var form by remember { mutableStateOf<MessForm?>(null) }
    var editedMember by remember { mutableStateOf<MemberEntity?>(null) }
    var deactivateTarget by remember { mutableStateOf<MemberEntity?>(null) }
    var showCalculator by rememberSaveable { mutableStateOf(false) }
    var quickMemberId by rememberSaveable { mutableStateOf<Int?>(null) }
    var quickCalculatorMemberId by rememberSaveable { mutableStateOf<Int?>(null) }

    val monthTime = (Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }).timeInMillis
    val bounds = monthBounds(monthTime)
    val monthDeposits = deposits.filter { it.date in bounds }
    val monthMeals = meals.filter { it.date in bounds }
    val monthBazar = bazar.filter { it.date in bounds }
    val monthPersonal = personal.filter { it.date in bounds }
    val totalBazar = monthBazar.sumOf { it.amount }
    val totalDeposits = monthDeposits.sumOf { it.amount }
    val cashInHand = totalDeposits - totalBazar
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
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onOpenDrawer, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Outlined.Menu, contentDescription = stringResource(R.string.open_menu))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        stringResource(R.string.section_bachelor),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(stringResource(R.string.mess_title), style = MaterialTheme.typography.headlineSmall)
                }
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
            ScrollableTabRow(
                selectedTabIndex = tab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                listOf(R.string.mess_calculation, R.string.summary, R.string.members, R.string.activity).forEachIndexed { index, label ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(stringResource(label)) })
                }
            }
        }
        when (tab) {
            0 -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatChip(stringResource(R.string.bazar_total), formatAmount(totalBazar, banglaDigits), CashOut, Modifier.weight(1f))
                            StatChip(stringResource(R.string.total_deposit), formatAmount(totalDeposits, banglaDigits), CashIn, Modifier.weight(1f))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val surplus = cashInHand >= 0
                            val fundTitle = if (surplus) stringResource(R.string.mess_cash_surplus) else stringResource(R.string.mess_cash_deficit)
                            StatChip(
                                label = fundTitle,
                                value = formatAmount(kotlin.math.abs(cashInHand), banglaDigits),
                                tint = if (surplus) CashIn else CashOut,
                                modifier = Modifier.weight(1f)
                            )
                            StatChip(
                                label = stringResource(R.string.meal_rate),
                                value = formatAmount((rate * 100).toLong(), banglaDigits),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { quickCalculatorMemberId = null; showCalculator = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.quick_calculator), maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                val summary = MessCalculator.calculateMessSummary(
                                    includedMembers.map { member ->
                                        MemberMealInput(
                                            memberId = member.id,
                                            memberName = member.name,
                                            deposits = monthDeposits.filter { it.memberId == member.id }.sumOf { it.amount },
                                            bazarSpending = monthBazar.filter { it.memberId == member.id }.sumOf { it.amount },
                                            meals = monthMeals.filter { it.memberId == member.id }.sumOf { it.totalMeals }
                                        )
                                    },
                                    totalBazar,
                                    totalDeposits
                                )
                                val report = MessCalculator.generateShareableReport(
                                    monthLabel(monthTime),
                                    summary,
                                    isBn = banglaDigits || Locale.getDefault().language == "bn"
                                )
                                clipboardManager.setText(AnnotatedString(report))
                                Toast.makeText(context, context.getString(R.string.mess_summary_copied), Toast.LENGTH_SHORT).show()
                                try {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, report)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.share_mess_summary))
                                    context.startActivity(shareIntent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.share_mess_summary), maxLines = 1)
                        }
                    }
                }
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { quickMemberId = null; form = MessForm.BAZAR },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.add_bazar), maxLines = 1)
                        }
                        Button(
                            onClick = { quickMemberId = null; form = MessForm.DEPOSIT },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.add_deposit), maxLines = 1)
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.all_members_calculation),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                if (settlements.isNotEmpty()) {
                    items(settlements, key = { "ledger-${it.memberId}" }) { s ->
                        MemberLedgerCard(
                            settlement = s,
                            banglaDigits = banglaDigits,
                            onCalculate = { quickCalculatorMemberId = s.memberId; showCalculator = true },
                            onAddDeposit = { quickMemberId = s.memberId; form = MessForm.DEPOSIT },
                            onAddBazar = { quickMemberId = s.memberId; form = MessForm.BAZAR }
                        )
                    }
                } else {
                    item { EmptyMessState(onAdd = { form = MessForm.MEMBER }) }
                }
            }
            1 -> {
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
                            Text(totalMeals.toCleanNumber(banglaDigits), style = MaterialTheme.typography.headlineMedium)
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
            2 -> {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        val activeCount = if (banglaDigits) com.finora.domain.time.toBanglaDigits("${members.count { it.isActive }}") else "${members.count { it.isActive }}"
                        Text(stringResource(R.string.active_members, activeCount), style = MaterialTheme.typography.titleLarge)
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

    if (showCalculator) {
        MessCalculatorDialog(
            members = members.filter { it.isActive },
            settlements = settlements,
            totalBazar = totalBazar,
            totalMeals = totalMeals,
            banglaDigits = banglaDigits,
            initialMemberId = quickCalculatorMemberId,
            onDismiss = {
                showCalculator = false
                quickCalculatorMemberId = null
            }
        )
    }

    if (form != null) {
        MessEntrySheet(
            viewModel = viewModel,
            kind = form!!,
            members = members.filter { it.isActive },
            editedMember = editedMember,
            initialMemberId = quickMemberId,
            onDismiss = { form = null; editedMember = null; quickMemberId = null }
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
private fun MemberLedgerCard(
    settlement: MemberSettlement,
    banglaDigits: Boolean,
    onCalculate: () -> Unit,
    onAddDeposit: () -> Unit,
    onAddBazar: () -> Unit
) {
    val isBn = banglaDigits || Locale.getDefault().language == "bn"
    val isRefund = settlement.balance > 0
    val isDue = settlement.balance < 0
    val totalPaid = settlement.deposits + settlement.bazarSpending
    val statusColor = if (isRefund) CashIn else if (isDue) CashOut else MaterialTheme.colorScheme.outline
    val statusText = if (isRefund) stringResource(R.string.due_status_gets_back)
        else if (isDue) stringResource(R.string.due_status_owes)
        else stringResource(R.string.due_status_settled)

    FinoraCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        shape = MaterialTheme.shapes.small,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            settlement.memberName.take(1).uppercase(),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(settlement.memberName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${settlement.meals.toCleanNumber(banglaDigits)} ${stringResource(R.string.meals)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRefund) CashIn.copy(alpha = 0.12f)
                        else if (isDue) CashOut.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.border(
                        1.dp,
                        statusColor.copy(alpha = 0.4f),
                        MaterialTheme.shapes.small
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            formatAmount(kotlin.math.abs(settlement.balance), banglaDigits),
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            statusText,
                            color = statusColor,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.member_deposit_paid), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatAmount(settlement.deposits, banglaDigits), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = CashIn)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.member_bazar_spent), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatAmount(settlement.bazarSpending, banglaDigits), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = CashOut)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.member_total_contribution), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatAmount(totalPaid, banglaDigits), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.member_meal_cost), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatAmount(settlement.mealCost, banglaDigits), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            val formulaDesc = if (isBn) {
                "হিসাব: মোট প্রদান (${formatAmount(totalPaid, banglaDigits)}) − মিল খরচ (${formatAmount(settlement.mealCost, banglaDigits)}) = ${formatAmount(settlement.balance, banglaDigits)}"
            } else {
                "Formula: Paid (${formatAmount(totalPaid, banglaDigits)}) − Meal Cost (${formatAmount(settlement.mealCost, banglaDigits)}) = ${formatAmount(settlement.balance, banglaDigits)}"
            }

            Text(
                formulaDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCalculate) {
                    Icon(Icons.Outlined.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(4.dp))
                    Text(if (isBn) "হিসাব" else "Calculate")
                }
                TextButton(onClick = onAddDeposit) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(4.dp))
                    Text(stringResource(R.string.quick_deposit))
                }
                TextButton(onClick = onAddBazar) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(4.dp))
                    Text(stringResource(R.string.quick_bazar))
                }
            }
        }
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
                    stringResource(R.string.meals_and_share, settlement.meals.toCleanNumber(banglaDigits), formatAmount(settlement.mealCost, banglaDigits)),
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

private fun Double.toCleanNumber(banglaDigits: Boolean = false): String {
    val formatted = if (this % 1.0 == 0.0) toInt().toString() else String.format(java.util.Locale.US, "%.1f", this)
    return if (banglaDigits) com.finora.domain.time.toBanglaDigits(formatted) else formatted
}
