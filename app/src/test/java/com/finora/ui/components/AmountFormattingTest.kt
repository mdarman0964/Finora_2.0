package com.finora.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AmountFormattingTest {

    @Test
    fun testFormatAmountEnglish() {
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            val result = formatAmount(150000L, banglaDigits = false)
            assertTrue("Expected result to start with $", result.startsWith("$"))
            assertTrue("Expected 1,500.00 in result", result.contains("1,500.00"))
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun testFormatAmountBangla() {
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale("bn", "BD"))
            val result = formatAmount(150000L, banglaDigits = true)
            assertTrue("Expected result to start with ৳", result.startsWith("৳"))
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun testCurrencySymbol() {
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            assertEquals("$", getCurrencySymbol())

            Locale.setDefault(Locale("bn"))
            assertEquals("৳", getCurrencySymbol())
        } finally {
            Locale.setDefault(originalLocale)
        }
    }
}
