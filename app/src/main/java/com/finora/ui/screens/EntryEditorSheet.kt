package com.finora.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.finora.R
import com.finora.data.local.CashEntryEntity
import com.finora.data.local.MemberEntity
import com.finora.ui.FinoraViewModel
import com.finora.ui.components.GradientButton
import com.finora.domain.time.dateLabel
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorSheet(
    viewModel: FinoraViewModel,
    bookId: Int,
    members: List<MemberEntity>,
    initialEntry: CashEntryEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    var type by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.type ?: "OUT") }
    var amount by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.amount?.let { BigDecimal(it).divide(BigDecimal(100)).stripTrailingZeros().toPlainString() } ?: "") }
    var category by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.category ?: "Food") }
    var note by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.note ?: "") }
    var selectedMember by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.memberId ?: 0) }
    var date by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.date ?: System.currentTimeMillis()) }
    var categoryMenu by remember { mutableStateOf(false) }
    var memberMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val categories = listOf("Food", "Transport", "Shopping", "Bills", "Salary", "Gift", "Health", "Other")
    val parsedAmount = amount.trim().replace(",", "").toBigDecimalOrNull()
        ?.multiply(BigDecimal(100))?.setScale(0, RoundingMode.HALF_UP)?.toLong()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(stringResource(if (initialEntry == null) R.string.add_entry else R.string.edit_entry), style = MaterialTheme.typography.headlineMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(selected = type == "IN", onClick = { type = "IN" }, label = { Text(stringResource(R.string.cash_in)) })
                FilterChip(selected = type == "OUT", onClick = { type = "OUT" }, label = { Text(stringResource(R.string.cash_out)) })
            }
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.amount_bdt)) },
                leadingIcon = { Text("৳", modifier = Modifier.padding(start = 12.dp), color = MaterialTheme.colorScheme.primary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            Column {
                Text(stringResource(R.string.category), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedButton(onClick = { categoryMenu = true }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text(categoryTitle(category))
                }
                DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                    categories.forEach { value ->
                        DropdownMenuItem(text = { Text(categoryTitle(value)) }, onClick = { category = value; categoryMenu = false })
                    }
                }
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.note_optional)) },
                singleLine = true
            )
            Column {
                Text(stringResource(R.string.member_optional), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedButton(onClick = { memberMenu = true }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text(if (selectedMember == 0) stringResource(R.string.no_member) else members.firstOrNull { it.id == selectedMember }?.name ?: stringResource(R.string.no_member))
                }
                DropdownMenu(expanded = memberMenu, onDismissRequest = { memberMenu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.no_member)) }, onClick = { selectedMember = 0; memberMenu = false })
                    members.forEach { member ->
                        DropdownMenuItem(text = { Text(member.name) }, onClick = { selectedMember = member.id; memberMenu = false })
                    }
                }
            }
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Text("${stringResource(R.string.date)} · ${dateLabel(date)}")
            }
            GradientButton(
                label = stringResource(R.string.save_entry),
                enabled = (parsedAmount ?: 0L) > 0L && bookId > 0,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val validAmount = parsedAmount ?: return@GradientButton
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    viewModel.saveEntry(
                        CashEntryEntity(
                            id = initialEntry?.id ?: 0,
                            bookId = bookId,
                            type = type,
                            amount = validAmount,
                            category = category,
                            note = note.trim(),
                            date = date,
                            memberId = selectedMember.takeIf { it > 0 }
                        )
                    )
                    onDismiss()
                }
            )
            if (initialEntry != null) {
                TextButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.delete_entry), color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.size(4.dp))
        }
    }

    if (showDatePicker) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { date = picker.selectedDateMillis ?: date; showDatePicker = false }) {
                    Text(stringResource(R.string.done))
                }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) { DatePicker(state = picker) }
    }

    if (showDeleteConfirmation && initialEntry != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteEntry(initialEntry.id); showDeleteConfirmation = false; onDismiss() }) {
                    Text(stringResource(R.string.delete_entry), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun categoryTitle(category: String): String = when (category) {
    "Food" -> stringResource(R.string.category_food)
    "Transport" -> stringResource(R.string.category_transport)
    "Shopping" -> stringResource(R.string.category_shopping)
    "Bills" -> stringResource(R.string.category_bills)
    "Salary" -> stringResource(R.string.category_salary)
    "Gift" -> stringResource(R.string.category_gift)
    "Health" -> stringResource(R.string.category_health)
    else -> stringResource(R.string.category_other)
}
