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
}
