package com.finora.domain.mess

import org.junit.Assert.assertEquals
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
}
