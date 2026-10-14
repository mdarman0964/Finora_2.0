package com.finora.domain.time

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun monthKey(timestamp: Long): String =
    SimpleDateFormat("yyyy-MM", Locale.US).format(Date(timestamp))

fun monthBounds(timestamp: Long): LongRange {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = calendar.timeInMillis
    calendar.add(Calendar.MONTH, 1)
    calendar.add(Calendar.MILLISECOND, -1)
    return start..calendar.timeInMillis
}

fun monthLabel(timestamp: Long, locale: Locale = Locale.getDefault()): String =
    SimpleDateFormat("MMMM yyyy", locale).format(Date(timestamp))

fun dateLabel(timestamp: Long, locale: Locale = Locale.getDefault()): String =
    SimpleDateFormat("EEE, d MMM", locale).format(Date(timestamp))
