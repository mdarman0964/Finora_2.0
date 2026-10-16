package com.finora.domain.mess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MessCalculatorTest {
    @Test
    fun mealRateDividesSharedBazarByTotalMealUnits() {
        assertEquals(125.0, MessCalculator.mealRate(totalBazar = 2_500, totalMeals = 20.0), 0.001)
    }

    @Test
    fun zeroMealsProducesZeroRate() {
        assertEquals(0.0, MessCalculator.mealRate(totalBazar = 2_500, totalMeals = 0.0), 0.0)
        assertEquals(0.0, MessCalculator.mealRate(totalBazar = 2_500, totalMeals = -1.0), 0.0)
    }

    @Test
    fun halfMealsAreChargedAtHalfRateAndBalanceUsesDepositsAndOwnBazar() {
        val member = MemberMealInput(
            memberId = 1,
            memberName = "Ari",
            deposits = 1_000,
            bazarSpending = 200,
            meals = 3.5
        )
        val result = MessCalculator.settle(listOf(member), totalBazar = 700).single()
        assertEquals(200.0, MessCalculator.mealRate(700, 3.5), 0.001)
        assertEquals(700L, result.mealCost)
        assertEquals(500L, result.balance)
    }

    @Test
    fun testCalculateMessSummaryAndSettlementDues() {
        val member1 = MemberMealInput(
            memberId = 1,
            memberName = "Arif",
            deposits = 300_000L, // 3000 taka
            bazarSpending = 200_000L, // 2000 taka
            meals = 50.0
        )
        val member2 = MemberMealInput(
            memberId = 2,
            memberName = "Rakib",
            deposits = 100_000L, // 1000 taka
            bazarSpending = 0L,
            meals = 50.0
        )

        val totalBazar = 200_000L // 2000 taka total bazar
        val totalDeposits = 400_000L // 4000 taka total deposits

        val summary = MessCalculator.calculateMessSummary(listOf(member1, member2), totalBazar, totalDeposits)

        // Total meals = 100
        assertEquals(100.0, summary.totalMeals, 0.001)
        // Meal rate = 200000 / 100 = 2000 paisa (20 taka) per meal
        assertEquals(2000.0, summary.mealRate, 0.001)
        // Cash in hand = 400000 - 200000 = 200000 (surplus)
        assertEquals(200_000L, summary.cashInHand)

        val s1 = summary.settlements.first { it.memberId == 1 }
        val s2 = summary.settlements.first { it.memberId == 2 }

        // Member 1 meal cost = 50 * 2000 = 100000 paisa (1000 taka)
        assertEquals(100_000L, s1.mealCost)
        // Member 1 paid = 300000 + 200000 = 500000 paisa (5000 taka)
        // Balance = 500000 - 100000 = +400000 paisa (+4000 taka refund / পাওনা)
        assertEquals(400_000L, s1.balance)

        // Member 2 meal cost = 50 * 2000 = 100000 paisa (1000 taka)
        // Member 2 paid = 100000 + 0 = 100000 paisa (1000 taka)
        // Balance = 100000 - 100000 = 0 (settled)
        assertEquals(0L, s2.balance)
    }

    @Test
    fun testGenerateShareableReportBengaliAndEnglish() {
        val member = MemberMealInput(
            memberId = 1,
            memberName = "রহিম",
            deposits = 200_000L,
            bazarSpending = 100_000L,
            meals = 10.0
        )
        val summary = MessCalculator.calculateMessSummary(listOf(member), totalBazar = 100_000L, totalDeposits = 200_000L)

        val bnReport = MessCalculator.generateShareableReport("অক্টোবর ২০২৬", summary, isBn = true)
        assertTrue(bnReport.contains("ব্যাচেলর মেস হিসাব বিবরণী"))
        assertTrue(bnReport.contains("মোট বাজার খরচ"))
        assertTrue(bnReport.contains("মোট মেম্বারের জমা"))
        assertTrue(bnReport.contains("রহিম"))

        val enReport = MessCalculator.generateShareableReport("October 2026", summary, isBn = false)
        assertTrue(enReport.contains("Bachelor Mess Statement"))
        assertTrue(enReport.contains("Total Bazar"))
        assertTrue(enReport.contains("Total Deposits"))
    }
}
