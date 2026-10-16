package com.finora.ui.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.finora.R
import com.finora.data.local.MemberEntity
import com.finora.domain.mess.MemberSettlement
import com.finora.domain.mess.MessCalculator
import com.finora.ui.components.FinoraCard
import com.finora.ui.components.formatAmount
import com.finora.ui.theme.CashIn
import com.finora.ui.theme.CashOut
import java.util.Locale
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessCalculatorDialog(
    members: List<MemberEntity>,
    settlements: List<MemberSettlement>,
    totalBazar: Long,
    totalMeals: Double,
    banglaDigits: Boolean,
    initialMemberId: Int? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isBn = banglaDigits || Locale.getDefault().language == "bn"

    val defaultMemberId = initialMemberId ?: members.firstOrNull()?.id
    var selectedMemberId by rememberSaveable { mutableStateOf<Int?>(defaultMemberId) }
    var memberDropdownExpanded by remember { mutableStateOf(false) }

    val initialSettlement = settlements.firstOrNull { it.memberId == selectedMemberId }
    var memberNameInput by rememberSaveable(selectedMemberId) {
        mutableStateOf(members.firstOrNull { it.id == selectedMemberId }?.name ?: "")
    }
    var bazarInput by rememberSaveable(selectedMemberId) {
        mutableStateOf(
            if (initialSettlement != null && initialSettlement.bazarSpending > 0)
                "%.2f".format(Locale.US, initialSettlement.bazarSpending / 100.0)
            else ""
        )
    }
    var depositInput by rememberSaveable(selectedMemberId) {
        mutableStateOf(
            if (initialSettlement != null && initialSettlement.deposits > 0)
                "%.2f".format(Locale.US, initialSettlement.deposits / 100.0)
            else ""
        )
    }
    var memberMealsInput by rememberSaveable(selectedMemberId) {
        mutableStateOf(
            if (initialSettlement != null && initialSettlement.meals > 0.0)
                if (initialSettlement.meals % 1.0 == 0.0) initialSettlement.meals.toInt().toString()
                else "%.1f".format(Locale.US, initialSettlement.meals)
            else ""
        )
    }
    var totalBazarInput by rememberSaveable {
        mutableStateOf(
            if (totalBazar > 0) "%.2f".format(Locale.US, totalBazar / 100.0) else ""
        )
    }
    var totalMealsInput by rememberSaveable {
        mutableStateOf(
            if (totalMeals > 0.0)
                if (totalMeals % 1.0 == 0.0) totalMeals.toInt().toString()
                else "%.1f".format(Locale.US, totalMeals)
            else ""
        )
    }

    val parsedTotalBazarPaisa = totalBazarInput.trim().replace(",", "").toDoubleOrNull()?.let { (it * 100).roundToLong() } ?: 0L
    val parsedTotalMeals = totalMealsInput.trim().toDoubleOrNull() ?: 0.0
    val parsedDepositPaisa = depositInput.trim().replace(",", "").toDoubleOrNull()?.let { (it * 100).roundToLong() } ?: 0L
    val parsedBazarPaisa = bazarInput.trim().replace(",", "").toDoubleOrNull()?.let { (it * 100).roundToLong() } ?: 0L
    val parsedMemberMeals = memberMealsInput.trim().toDoubleOrNull() ?: 0.0

    val calcRate = MessCalculator.mealRate(parsedTotalBazarPaisa, parsedTotalMeals)
    val calcTotalPaid = parsedDepositPaisa + parsedBazarPaisa
    val calcMealCost = (parsedMemberMeals * calcRate).roundToLong()
    val calcBalance = parsedDepositPaisa + parsedBazarPaisa - calcMealCost

    val effectiveName = memberNameInput.ifBlank {
        members.firstOrNull { it.id == selectedMemberId }?.name
            ?: if (isBn) "মেম্বার" else "Member"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        stringResource(R.string.calculator_dialog_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.close))
                }
            }

            Text(
                stringResource(R.string.calculator_dialog_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Member Selection Dropdown Menu (No typing required)
            if (members.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isBn) "সদস্য নির্বাচন (ড্রপডাউন)" else "Select Member (Dropdown)",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = members.firstOrNull { it.id == selectedMemberId }?.name
                                ?: if (selectedMemberId == null && memberNameInput.isNotBlank()) memberNameInput
                                else if (isBn) "তালিকা থেকে সদস্য সিলেক্ট করুন" else "Select member from list",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.member_name)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { memberDropdownExpanded = true }) {
                                    Icon(Icons.Outlined.ExpandMore, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Invisible clickable overlay to ensure the entire input field triggers dropdown
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { memberDropdownExpanded = true }
                        )

                        DropdownMenu(
                            expanded = memberDropdownExpanded,
                            onDismissRequest = { memberDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            members.forEach { member ->
                                val isSelected = member.id == selectedMemberId
                                val memberSettlement = settlements.firstOrNull { it.memberId == member.id }
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Person,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = member.name,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (memberSettlement != null) {
                                                val sText = if (isBn) {
                                                    "জমা: ${formatAmount(memberSettlement.deposits, true)} | বাজার: ${formatAmount(memberSettlement.bazarSpending, true)}"
                                                } else {
                                                    "Dep: ${formatAmount(memberSettlement.deposits, false)} | Bazar: ${formatAmount(memberSettlement.bazarSpending, false)}"
                                                }
                                                Text(
                                                    text = sText,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    trailingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                Icons.Outlined.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        selectedMemberId = member.id
                                        memberNameInput = member.name
                                        val s = settlements.firstOrNull { it.memberId == member.id }
                                        if (s != null) {
                                            depositInput = if (s.deposits > 0) "%.2f".format(Locale.US, s.deposits / 100.0) else ""
                                            bazarInput = if (s.bazarSpending > 0) "%.2f".format(Locale.US, s.bazarSpending / 100.0) else ""
                                            memberMealsInput = if (s.meals > 0.0) {
                                                if (s.meals % 1.0 == 0.0) s.meals.toInt().toString() else "%.1f".format(Locale.US, s.meals)
                                            } else ""
                                        } else {
                                            depositInput = ""
                                            bazarInput = ""
                                            memberMealsInput = ""
                                        }
                                        memberDropdownExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Calculate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.custom_calculation),
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    selectedMemberId = null
                                    memberNameInput = ""
                                    depositInput = ""
                                    bazarInput = ""
                                    memberMealsInput = ""
                                    memberDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = memberNameInput,
                    onValueChange = { memberNameInput = it },
                    label = { Text(stringResource(R.string.member_name)) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (selectedMemberId == null && members.isNotEmpty()) {
                OutlinedTextField(
                    value = memberNameInput,
                    onValueChange = { memberNameInput = it },
                    label = { Text(if (isBn) "কাস্টম মেম্বারের নাম লিখুন (ঐচ্ছিক)" else "Custom Member Name (Optional)") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            FinoraCard {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (isBn) "১. মেসের মোট খরচ ও মিল" else "1. Total Mess Expenses & Meals",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = totalBazarInput,
                            onValueChange = { totalBazarInput = it },
                            label = { Text(stringResource(R.string.bazar_total)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = totalMealsInput,
                            onValueChange = { totalMealsInput = it },
                            label = { Text(stringResource(R.string.month_meals)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    val rateDisplay = formatAmount((calcRate * 100).roundToLong(), banglaDigits)
                    Text(
                        if (isBn) "স্বয়ংক্রিয় মিল রেট: $rateDisplay / মিল" else "Calculated Meal Rate: $rateDisplay / meal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FinoraCard {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (isBn) "২. $effectiveName-এর জমা, বাজার ও মিল" else "2. $effectiveName\'s Deposit, Bazar & Meals",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = depositInput,
                            onValueChange = { depositInput = it },
                            label = { Text(stringResource(R.string.member_deposit_paid)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = bazarInput,
                            onValueChange = { bazarInput = it },
                            label = { Text(stringResource(R.string.member_bazar_spent)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = memberMealsInput,
                        onValueChange = { memberMealsInput = it },
                        label = { Text(if (isBn) "মেম্বারের মিল সংখ্যা" else "Member Meals") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            val isRefund = calcBalance > 0
            val isDue = calcBalance < 0
            val statusColor = if (isRefund) CashIn else if (isDue) CashOut else MaterialTheme.colorScheme.outline
            val statusBorderColor = if (isRefund) CashIn.copy(alpha = 0.5f) else if (isDue) CashOut.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            val resultBgColor = if (isRefund) CashIn.copy(alpha = 0.08f) else if (isDue) CashOut.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

            val mainResultText = if (isRefund) {
                stringResource(R.string.calc_result_gets_back, effectiveName, formatAmount(calcBalance, banglaDigits))
            } else if (isDue) {
                stringResource(R.string.calc_result_owes, effectiveName, formatAmount(kotlin.math.abs(calcBalance), banglaDigits))
            } else {
                if (isBn) "$effectiveName-এর কোনো দেনা বা পাওনা নেই (হিসাব সমান)।"
                else "$effectiveName is fully settled with zero balance."
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, statusBorderColor, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = resultBgColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (isRefund) stringResource(R.string.due_status_gets_back)
                            else if (isDue) stringResource(R.string.due_status_owes)
                            else stringResource(R.string.due_status_settled),
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            formatAmount(kotlin.math.abs(calcBalance), banglaDigits),
                            color = statusColor,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        mainResultText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isBn)
                            "• মোট দেওয়া টাকা: ${formatAmount(calcTotalPaid, banglaDigits)} (জমা: ${formatAmount(parsedDepositPaisa, banglaDigits)} + বাজার: ${formatAmount(parsedBazarPaisa, banglaDigits)})\n" +
                            "• মিল খরচ: ${formatAmount(calcMealCost, banglaDigits)} ($parsedMemberMeals মিল × ${formatAmount((calcRate * 100).roundToLong(), banglaDigits)})\n" +
                            "• হিসাব: ${formatAmount(calcTotalPaid, banglaDigits)} − ${formatAmount(calcMealCost, banglaDigits)} = ${formatAmount(calcBalance, banglaDigits)}"
                        else
                            "• Total paid: ${formatAmount(calcTotalPaid, banglaDigits)} (Deposit: ${formatAmount(parsedDepositPaisa, banglaDigits)} + Bazar: ${formatAmount(parsedBazarPaisa, banglaDigits)})\n" +
                            "• Meal cost: ${formatAmount(calcMealCost, banglaDigits)} ($parsedMemberMeals meals × ${formatAmount((calcRate * 100).roundToLong(), banglaDigits)})\n" +
                            "• Calculation: ${formatAmount(calcTotalPaid, banglaDigits)} − ${formatAmount(calcMealCost, banglaDigits)} = ${formatAmount(calcBalance, banglaDigits)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val shareText = if (isBn) {
                            """
                            🧮 মেস দেনা-পাওনা হিসাব: $effectiveName
                            ----------------------------
                            🛒 বাজার খরচ: ${formatAmount(parsedBazarPaisa, banglaDigits)}
                            💰 জমা দেওয়া টাকা: ${formatAmount(parsedDepositPaisa, banglaDigits)}
                            💵 মোট প্রদান: ${formatAmount(calcTotalPaid, banglaDigits)}
                            🍽️ মিল খরচ: ${formatAmount(calcMealCost, banglaDigits)} ($parsedMemberMeals মিল)
                            👉 ফলাফল: $mainResultText
                            ----------------------------
                            হিসাব: Finora App
                            """.trimIndent()
                        } else {
                            """
                            🧮 Mess Due Calculation: $effectiveName
                            ----------------------------
                            🛒 Bazar Spent: ${formatAmount(parsedBazarPaisa, banglaDigits)}
                            💰 Deposit Paid: ${formatAmount(parsedDepositPaisa, banglaDigits)}
                            💵 Total Given: ${formatAmount(calcTotalPaid, banglaDigits)}
                            🍽️ Meal Cost: ${formatAmount(calcMealCost, banglaDigits)} ($parsedMemberMeals meals)
                            👉 Result: $mainResultText
                            ----------------------------
                            Calculated by Finora App
                            """.trimIndent()
                        }
                        clipboardManager.setText(AnnotatedString(shareText))
                        Toast.makeText(context, context.getString(R.string.mess_summary_copied), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(if (isBn) "কপি করুন" else "Copy")
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}
