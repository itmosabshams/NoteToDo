package com.shams.notetodo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.shams.notetodo.R

// تعریف خانواده فونت Negar
val YekanFontFamily = FontFamily(
    Font(R.font.yekan, FontWeight.Normal)
)

// تنظیم تایپوگرافی سراسری با فونت Negar
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = YekanFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = YekanFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = YekanFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = YekanFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = YekanFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = YekanFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = YekanFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)
