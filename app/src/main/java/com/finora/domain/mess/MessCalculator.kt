package com.finora.domain.mess

import kotlin.math.roundToLong

data class MemberMealInput(
    val memberId: Int,
    val memberName: String,
    val deposits: Long,
    val bazarSpending: Long,
    val meals: Double
)

data class MemberSettlement(
    val memberId: Int,
    val memberName: String,
    val deposits: Long,
    val bazarSpending: Long,
    val meals: Double,
    val mealCost: Long,
    val balance: Long
)

object MessCalculator {
    fun mealRate(totalBazar: Long, totalMeals: Double): Double =
        if (totalMeals <= 0.0) 0.0 else totalBazar.toDouble() / totalMeals

    fun memberBalance(deposits: Long, ownBazarSpending: Long, meals: Double, rate: Double): Long =
        deposits + ownBazarSpending - (meals * rate).roundToLong()

    fun settle(members: List<MemberMealInput>, totalBazar: Long): List<MemberSettlement> {
        val totalMeals = members.sumOf { it.meals }
        val rate = mealRate(totalBazar, totalMeals)
        return members.map { member ->
            val mealCost = (member.meals * rate).roundToLong()
            MemberSettlement(
                memberId = member.memberId,
                memberName = member.memberName,
                deposits = member.deposits,
                bazarSpending = member.bazarSpending,
                meals = member.meals,
                mealCost = mealCost,
                balance = memberBalance(member.deposits, member.bazarSpending, member.meals, rate)
            )
        }
    }

    fun calculateMessSummary(
        members: List<MemberMealInput>,
        totalBazar: Long,
        totalDeposits: Long
    ): MessSummary {
        val settlements = settle(members, totalBazar)
        val totalMeals = members.sumOf { it.meals }
        val rate = mealRate(totalBazar, totalMeals)
        val cashInHand = totalDeposits - totalBazar
        return MessSummary(
            totalBazar = totalBazar,
            totalDeposits = totalDeposits,
            cashInHand = cashInHand,
            totalMeals = totalMeals,
            mealRate = rate,
            settlements = settlements
        )
    }

    fun formatMoneyString(amountInPaisa: Long, isBn: Boolean): String {
        val symbol = if (isBn) "৳" else "$"
        val amount = amountInPaisa / 100.0
        val formatted = String.format(java.util.Locale.US, "%.2f", amount)
        val withDigits = if (isBn) com.finora.domain.time.toBanglaDigits(formatted) else formatted
        return "$symbol$withDigits"
    }

    fun generateShareableReport(
        monthLabel: String,
        summary: MessSummary,
        isBn: Boolean
    ): String {
        val sb = StringBuilder()
        if (isBn) {
            sb.appendLine("📋 ব্যাচেলর মেস হিসাব বিবরণী · $monthLabel")
            sb.appendLine("─────────────────────────")
            sb.appendLine("🛒 মোট বাজার খরচ: ${formatMoneyString(summary.totalBazar, true)}")
            sb.appendLine("💰 মোট মেম্বারের জমা: ${formatMoneyString(summary.totalDeposits, true)}")
            val cashText = if (summary.cashInHand >= 0) {
                "${formatMoneyString(summary.cashInHand, true)} (উদ্বৃত্ত ক্যাশ)"
            } else {
                "${formatMoneyString(-summary.cashInHand, true)} (ঘাটতি)"
            }
            sb.appendLine("💵 মেস ক্যাশ ফান্ড: $cashText")
            val mealsStr = com.finora.domain.time.toBanglaDigits(
                if (summary.totalMeals % 1.0 == 0.0) summary.totalMeals.toInt().toString()
                else String.format(java.util.Locale.US, "%.1f", summary.totalMeals)
            )
            val rateStr = formatMoneyString((summary.mealRate * 100).roundToLong(), true)
            sb.appendLine("🍽️ মোট মিল: $mealsStr টি | মিল রেট: $rateStr")
            sb.appendLine()
            sb.appendLine("👥 মেম্বারদের দেনা-পাওনার হিসাব:")
            summary.settlements.forEachIndexed { index, s ->
                val num = com.finora.domain.time.toBanglaDigits("${index + 1}")
                val mMeals = com.finora.domain.time.toBanglaDigits(
                    if (s.meals % 1.0 == 0.0) s.meals.toInt().toString()
                    else String.format(java.util.Locale.US, "%.1f", s.meals)
                )
                val totalPaid = s.deposits + s.bazarSpending
                sb.appendLine("$num. ${s.memberName}:")
                sb.appendLine("   • জমা: ${formatMoneyString(s.deposits, true)} | বাজার: ${formatMoneyString(s.bazarSpending, true)} (মোট প্রদান: ${formatMoneyString(totalPaid, true)})")
                sb.appendLine("   • মিল: $mMeals টি (মিল খরচ: ${formatMoneyString(s.mealCost, true)})")
                if (s.balance > 0) {
                    sb.appendLine("   👉 পাওনা (মেস ফেরত দেবে): ${formatMoneyString(s.balance, true)}")
                } else if (s.balance < 0) {
                    sb.appendLine("   👉 দেনা (মেসে দিতে হবে): ${formatMoneyString(-s.balance, true)}")
                } else {
                    sb.appendLine("   👉 পরিশোধিত (ব্যালেন্স সমান)")
                }
            }
            sb.appendLine("─────────────────────────")
            sb.appendLine("হিসাব প্রস্তুত: Finora App")
        } else {
            sb.appendLine("📋 Bachelor Mess Statement · $monthLabel")
            sb.appendLine("─────────────────────────")
            sb.appendLine("🛒 Total Bazar: ${formatMoneyString(summary.totalBazar, false)}")
            sb.appendLine("💰 Total Deposits: ${formatMoneyString(summary.totalDeposits, false)}")
            val cashText = if (summary.cashInHand >= 0) {
                "${formatMoneyString(summary.cashInHand, false)} (Cash in hand)"
            } else {
                "${formatMoneyString(-summary.cashInHand, false)} (Deficit)"
            }
            sb.appendLine("💵 Mess Cash Fund: $cashText")
            val mealsStr = if (summary.totalMeals % 1.0 == 0.0) summary.totalMeals.toInt().toString()
                else String.format(java.util.Locale.US, "%.1f", summary.totalMeals)
            val rateStr = formatMoneyString((summary.mealRate * 100).roundToLong(), false)
            sb.appendLine("🍽️ Total Meals: $mealsStr | Meal Rate: $rateStr")
            sb.appendLine()
            sb.appendLine("👥 Members Settlement & Dues:")
            summary.settlements.forEachIndexed { index, s ->
                val mMeals = if (s.meals % 1.0 == 0.0) s.meals.toInt().toString()
                    else String.format(java.util.Locale.US, "%.1f", s.meals)
                val totalPaid = s.deposits + s.bazarSpending
                sb.appendLine("${index + 1}. ${s.memberName}:")
                sb.appendLine("   • Deposit: ${formatMoneyString(s.deposits, false)} | Bazar: ${formatMoneyString(s.bazarSpending, false)} (Total Paid: ${formatMoneyString(totalPaid, false)})")
                sb.appendLine("   • Meals: $mMeals (Meal Cost: ${formatMoneyString(s.mealCost, false)})")
                if (s.balance > 0) {
                    sb.appendLine("   👉 Gets back (Refund): ${formatMoneyString(s.balance, false)}")
                } else if (s.balance < 0) {
                    sb.appendLine("   👉 Owes to mess (Due): ${formatMoneyString(-s.balance, false)}")
                } else {
                    sb.appendLine("   👉 Settled (Balance 0)")
                }
            }
            sb.appendLine("─────────────────────────")
            sb.appendLine("Generated by Finora App")
        }
        return sb.toString().trimEnd()
    }
}

data class MessSummary(
    val totalBazar: Long,
    val totalDeposits: Long,
    val cashInHand: Long,
    val totalMeals: Double,
    val mealRate: Double,
    val settlements: List<MemberSettlement>
)
