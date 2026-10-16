package com.finora.domain.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class DateRangesAndFormattingTest {

    @Test
    fun testToBanglaDigits() {
        val englishNumbers = "0123456789"
        val expectedBangla = "০১২৩৪৫৬৭৮৯"
        assertEquals(expectedBangla, toBanglaDigits(englishNumbers))
    }

    @Test
    fun testToBanglaDigitsMixedText() {
        val input = "Amount: 1,500.50"
        val expected = "Amount: ১,৫০০.৫০"
        assertEquals(expected, toBanglaDigits(input))
    }

    @Test
    fun testMonthBounds() {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(2026, java.util.Calendar.JANUARY, 15, 12, 0, 0)
        val bounds = monthBounds(calendar.timeInMillis)
        
        val startCal = java.util.Calendar.getInstance().apply { timeInMillis = bounds.first }
        assertEquals(1, startCal.get(java.util.Calendar.DAY_OF_MONTH))
        assertEquals(java.util.Calendar.JANUARY, startCal.get(java.util.Calendar.MONTH))
        assertEquals(0, startCal.get(java.util.Calendar.HOUR_OF_DAY))

        val endCal = java.util.Calendar.getInstance().apply { timeInMillis = bounds.last }
        assertEquals(31, endCal.get(java.util.Calendar.DAY_OF_MONTH))
        assertEquals(java.util.Calendar.JANUARY, endCal.get(java.util.Calendar.MONTH))
    }

    @Test
    fun testMonthLabelEnglishAndBangla() {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(2026, java.util.Calendar.MARCH, 1)
        val time = calendar.timeInMillis

        val enLabel = monthLabel(time, Locale.US)
        assertEquals(true, enLabel.contains("2026"))

        val bnLabel = monthLabel(time, Locale("bn"))
        assertEquals(true, bnLabel.contains("২০২৬"))
    }
}
