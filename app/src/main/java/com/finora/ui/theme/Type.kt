package com.finora.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.finora.R

private val ManropeFamily = FontFamily(
    Font(R.font.manrope, FontWeight.Normal),
    Font(R.font.manrope, FontWeight.Bold)
)

private val BengaliFamily = FontFamily(
    Font(R.font.noto_sans_bengali, FontWeight.Normal),
    Font(R.font.noto_sans_bengali, FontWeight.Bold)
)

private fun createFinoraTypography(family: FontFamily) = Typography(
    displayLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 38.sp, lineHeight = 48.sp),
    headlineMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 21.sp)
)

val FinoraTypography = createFinoraTypography(ManropeFamily)
val FinoraBengaliTypography = createFinoraTypography(BengaliFamily)
