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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.finora.R
import com.finora.data.local.MemberEntity
import com.finora.domain.time.dateLabel
import com.finora.ui.FinoraViewModel
import com.finora.ui.components.GradientButton
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessEntrySheet(
    viewModel: FinoraViewModel,
    kind: MessForm,
    members: List<MemberEntity>,
    editedMember: MemberEntity?,
    initialMemberId: Int? = null,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    var activeKind by rememberSaveable(kind, editedMember?.id) { mutableStateOf(kind) }
    var name by rememberSaveable(editedMember?.id) { mutableStateOf(editedMember?.name ?: "") }
    var memberId by rememberSaveable(activeKind, editedMember?.id, initialMemberId) {
        mutableStateOf(initialMemberId ?: members.firstOrNull()?.id ?: 0)
    }
    var amount by rememberSaveable(activeKind) { mutableStateOf("") }
    var note by rememberSaveable(activeKind) { mutableStateOf("") }
    var category by rememberSaveable(activeKind) { mutableStateOf("Other") }
    var breakfast by rememberSaveable(activeKind) { mutableStateOf("") }
    var lunch by rememberSaveable(activeKind) { mutableStateOf("") }
    var dinner by rememberSaveable(activeKind) { mutableStateOf("") }
    var guest by rememberSaveable(activeKind) { mutableStateOf("") }
    var date by rememberSaveable(activeKind) { mutableStateOf(System.currentTimeMillis()) }
    var memberMenu by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val moneyAmount = amount.trim().replace(",", "").toBigDecimalOrNull()
        ?.multiply(BigDecimal(100))?.setScale(0, RoundingMode.HALF_UP)?.toLong()
    val mealValues = listOf(breakfast, lunch, dinner, guest).map { it.trim().toDoubleOrNull() ?: 0.0 }
    val hasMeals = mealValues.any { it > 0.0 } && mealValues.all { it >= 0.0 }

    val title = stringResource(
        when (activeKind) {
            MessForm.MEMBER -> if (editedMember == null) R.string.add_member else R.string.edit_member
            MessForm.DEPOSIT -> R.string.add_deposit
            MessForm.MEAL -> R.string.log_meals
            MessForm.BAZAR -> R.string.add_bazar
            MessForm.PERSONAL -> R.string.add_personal_expense
        }
    )

    val canSave = when (activeKind) {
        MessForm.MEMBER -> name.isNotBlank()
        MessForm.MEAL -> memberId > 0 && hasMeals
        else -> memberId > 0 && (moneyAmount ?: 0L) > 0L
    }

    val currSymbol = if (Locale.getDefault().language == "bn") "৳" else "$"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            if (editedMember == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeKind == MessForm.DEPOSIT,
                        onClick = { activeKind = MessForm.DEPOSIT },
                        label = { Text(stringResource(R.string.add_deposit)) },
                        leadingIcon = { Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = activeKind == MessForm.BAZAR,
                        onClick = { activeKind = MessForm.BAZAR },
                        label = { Text(stringResource(R.string.add_bazar)) },
                        leadingIcon = { Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = activeKind == MessForm.MEAL,
                        onClick = { activeKind = MessForm.MEAL },
                        label = { Text(stringResource(R.string.log_meals)) },
                        leadingIcon = { Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = activeKind == MessForm.MEMBER,
                        onClick = { activeKind = MessForm.MEMBER },
                        label = { Text(stringResource(R.string.add_member)) },
                        leadingIcon = { Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            if (activeKind == MessForm.MEMBER) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.member_name)) },
                    singleLine = true
                )
            } else {
                // 1. সদস্যের নাম (Member selection dropdown)
                Column {
                    Text(
                        stringResource(R.string.member_name),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedButton(
                        onClick = { memberMenu = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    members.firstOrNull { it.id == memberId }?.name ?: stringResource(R.string.choose_member),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = memberMenu,
                        onDismissRequest = { memberMenu = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name, fontWeight = if (member.id == memberId) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    memberId = member.id
                                    memberMenu = false
                                }
                            )
                        }
                    }
                }

                // 2. তারিখ (Date selection)
                Column {
                    Text(
                        stringResource(R.string.date),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    dateLabel(date),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
                        }
                    }
                }

                // 3. For DEPOSIT, BAZAR, PERSONAL -> Amount field
                if (activeKind == MessForm.DEPOSIT || activeKind == MessForm.BAZAR || activeKind == MessForm.PERSONAL) {
                    val amountLabel = when (activeKind) {
                        MessForm.DEPOSIT -> stringResource(R.string.deposit_amount)
                        MessForm.BAZAR -> stringResource(R.string.bazar_expense_amount)
                        else -> stringResource(R.string.amount_bdt)
                    }
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(amountLabel) },
                        placeholder = { Text("0.00") },
                        leadingIcon = {
                            Text(
                                currSymbol,
                                modifier = Modifier.padding(start = 12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                // For MEALS -> breakfast, lunch, dinner, guest
                if (activeKind == MessForm.MEAL) {
                    Text(
                        stringResource(R.string.meal_units_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        MealField(stringResource(R.string.breakfast), breakfast, { breakfast = it }, Modifier.weight(1f))
                        MealField(stringResource(R.string.lunch), lunch, { lunch = it }, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        MealField(stringResource(R.string.dinner), dinner, { dinner = it }, Modifier.weight(1f))
                        MealField(stringResource(R.string.guest_meals), guest, { guest = it }, Modifier.weight(1f))
                    }
                }

                // For PERSONAL -> category dropdown
                if (activeKind == MessForm.PERSONAL) {
                    Column {
                        Text(
                            stringResource(R.string.category),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedButton(onClick = { categoryMenu = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(personalCategory(category))
                        }
                        DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                            listOf("Food", "Transport", "Shopping", "Bills", "Health", "Other").forEach { value ->
                                DropdownMenuItem(
                                    text = { Text(personalCategory(value)) },
                                    onClick = { category = value; categoryMenu = false }
                                )
                            }
                        }
                    }
                }

                // Optional note field for DEPOSIT, BAZAR, PERSONAL
                if (activeKind == MessForm.DEPOSIT || activeKind == MessForm.BAZAR || activeKind == MessForm.PERSONAL) {
                    val noteLabel = if (activeKind == MessForm.BAZAR) {
                        stringResource(R.string.bazar_note_hint)
                    } else {
                        stringResource(R.string.note_optional)
                    }
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(noteLabel) },
                        singleLine = true
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            GradientButton(
                label = stringResource(R.string.save),
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    when (activeKind) {
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
