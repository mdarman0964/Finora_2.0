package com.finora.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.finora.R
import com.finora.data.local.MemberEntity
import com.finora.domain.time.dateLabel
import com.finora.ui.FinoraViewModel
import com.finora.ui.components.GradientButton
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessEntrySheet(
    viewModel: FinoraViewModel,
    kind: MessForm,
    members: List<MemberEntity>,
    editedMember: MemberEntity?,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    var name by rememberSaveable(editedMember?.id) { mutableStateOf(editedMember?.name ?: "") }
    var memberId by rememberSaveable(kind, editedMember?.id) { mutableStateOf(members.firstOrNull()?.id ?: 0) }
    var amount by rememberSaveable(kind) { mutableStateOf("") }
    var note by rememberSaveable(kind) { mutableStateOf("") }
    var category by rememberSaveable(kind) { mutableStateOf("Other") }
    var breakfast by rememberSaveable(kind) { mutableStateOf("") }
    var lunch by rememberSaveable(kind) { mutableStateOf("") }
    var dinner by rememberSaveable(kind) { mutableStateOf("") }
    var guest by rememberSaveable(kind) { mutableStateOf("") }
    var date by rememberSaveable(kind) { mutableStateOf(System.currentTimeMillis()) }
    var memberMenu by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val moneyAmount = amount.trim().replace(",", "").toBigDecimalOrNull()
        ?.multiply(BigDecimal(100))?.setScale(0, RoundingMode.HALF_UP)?.toLong()
    val mealValues = listOf(breakfast, lunch, dinner, guest).map { it.trim().toDoubleOrNull() ?: 0.0 }
    val hasMeals = mealValues.any { it > 0.0 } && mealValues.all { it >= 0.0 }
    val title = stringResource(
        when (kind) {
            MessForm.MEMBER -> if (editedMember == null) R.string.add_member else R.string.edit_member
            MessForm.DEPOSIT -> R.string.add_deposit
            MessForm.MEAL -> R.string.log_meals
            MessForm.BAZAR -> R.string.add_bazar
            MessForm.PERSONAL -> R.string.add_personal_expense
        }
    )
    val canSave = when (kind) {
        MessForm.MEMBER -> name.isNotBlank()
        MessForm.MEAL -> memberId > 0 && hasMeals
        else -> memberId > 0 && (moneyAmount ?: 0L) > 0L
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            if (kind == MessForm.MEMBER) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.member_name)) },
                    singleLine = true
                )
            } else {
                Column {
                    Text(stringResource(R.string.member), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedButton(onClick = { memberMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(members.firstOrNull { it.id == memberId }?.name ?: stringResource(R.string.choose_member))
                    }
                    DropdownMenu(expanded = memberMenu, onDismissRequest = { memberMenu = false }) {
                        members.forEach { member ->
                            DropdownMenuItem(text = { Text(member.name) }, onClick = { memberId = member.id; memberMenu = false })
                        }
                    }
                }
                if (kind == MessForm.MEAL) {
                    Text(stringResource(R.string.meal_units_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        MealField(stringResource(R.string.breakfast), breakfast, { breakfast = it }, Modifier.weight(1f))
                        MealField(stringResource(R.string.lunch), lunch, { lunch = it }, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        MealField(stringResource(R.string.dinner), dinner, { dinner = it }, Modifier.weight(1f))
                        MealField(stringResource(R.string.guest_meals), guest, { guest = it }, Modifier.weight(1f))
                    }
                } else {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.amount_bdt)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    if (kind == MessForm.PERSONAL) {
                        Column {
                            Text(stringResource(R.string.category), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 6.dp))
                            OutlinedButton(onClick = { categoryMenu = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(personalCategory(category))
                            }
                            DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                                listOf("Food", "Transport", "Shopping", "Bills", "Health", "Other").forEach { value ->
                                    DropdownMenuItem(text = { Text(personalCategory(value)) }, onClick = { category = value; categoryMenu = false })
                                }
                            }
                        }
                    }
                    if (kind == MessForm.BAZAR || kind == MessForm.PERSONAL) {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.note_optional)) },
                            singleLine = true
                        )
                    }
                }
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("${stringResource(R.string.date)} · ${dateLabel(date)}")
                }
            }
            GradientButton(
                label = stringResource(R.string.save),
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    when (kind) {
                        MessForm.MEMBER -> viewModel.saveMember(
                            MemberEntity(
                                id = editedMember?.id ?: 0,
                                name = name.trim(),
                                isActive = editedMember?.isActive ?: true,
                                createdAt = editedMember?.createdAt ?: System.currentTimeMillis()
                            )
                        )
                        MessForm.DEPOSIT -> viewModel.saveDeposit(memberId, moneyAmount ?: 0L, date)
                        MessForm.MEAL -> viewModel.saveMeal(memberId, date, mealValues[0], mealValues[1], mealValues[2], mealValues[3])
                        MessForm.BAZAR -> viewModel.saveBazar(memberId, moneyAmount ?: 0L, note.trim(), date)
                        MessForm.PERSONAL -> viewModel.savePersonalExpense(memberId, moneyAmount ?: 0L, category, note.trim(), date)
                    }
                    onDismiss()
                }
            )
        }
    }

    if (showDatePicker) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { date = picker.selectedDateMillis ?: date; showDatePicker = false }) { Text(stringResource(R.string.done)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) } }
        ) { DatePicker(state = picker) }
    }
}

@Composable
private fun MealField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        placeholder = { Text("0") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true
    )
}

@Composable
private fun personalCategory(value: String): String = when (value) {
    "Food" -> stringResource(R.string.category_food)
    "Transport" -> stringResource(R.string.category_transport)
    "Shopping" -> stringResource(R.string.category_shopping)
    "Bills" -> stringResource(R.string.category_bills)
    "Health" -> stringResource(R.string.category_health)
    else -> stringResource(R.string.category_other)
}
