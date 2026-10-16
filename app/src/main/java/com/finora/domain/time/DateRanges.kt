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

fun toBanglaDigits(text: String): String {
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder()
    for (ch in text) {
        if (ch in '0'..'9') {
            sb.append(bnDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

fun monthLabel(timestamp: Long, locale: Locale = Locale.getDefault()): String {
    val formatted = SimpleDateFormat("MMMM yyyy", locale).format(Date(timestamp))
    return if (locale.language == "bn") toBanglaDigits(formatted) else formatted
}

fun dateLabel(timestamp: Long, locale: Locale = Locale.getDefault()): String {
    val formatted = SimpleDateFormat("EEE, d MMM", locale).format(Date(timestamp))
    return if (locale.language == "bn") toBanglaDigits(formatted) else formatted
}
