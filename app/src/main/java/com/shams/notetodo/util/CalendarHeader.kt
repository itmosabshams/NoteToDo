package com.shams.notetodo.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import ir.huri.jcal.JalaliCalendar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarHeader() {
    var dateInfo by remember { mutableStateOf(getDateInfo()) }

    LaunchedEffect(Unit) {
        while (true) {
            dateInfo = getDateInfo()
            delay(60_000L)
        }
    }

    // رنگ‌های حرفه‌ای‌تر با گرادینت
    val gradientStart = Color(0xFF03A9F4)
    val gradientEnd = Color(0xFF0288D1)
    val waveColor = Color(0xFF0277BD).copy(alpha = 0.8f)
    val waveColorLight = Color(0xFFB3E5FC).copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // گرادینت عمودی برای پس‌زمینه
            val backgroundGradient = Brush.verticalGradient(
                colors = listOf(gradientStart, gradientEnd)
            )
            drawRect(brush = backgroundGradient, size = Size(width, height))

            // موج اصلی پایین
            val wavePath = Path().apply {
                moveTo(0f, height * 0.75f)
                quadraticBezierTo(width / 2, height, width, height * 0.75f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(path = wavePath, color = waveColor)

            // موج دوم کوچک‌تر برای عمق بیشتر
            val wavePath2 = Path().apply {
                moveTo(0f, height * 0.85f)
                quadraticBezierTo(width / 3, height * 0.95f, width / 2, height * 0.85f)
                quadraticBezierTo(width * 2 / 3, height * 0.75f, width, height * 0.85f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(path = wavePath2, color = waveColorLight)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // روز هفته - بالا سمت راست (با فونت کمی بزرگتر)
            Text(
                text = dateInfo.dayOfWeek,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.TopStart),
                textAlign = TextAlign.Right,
                letterSpacing = 0.5.sp
            )

            // عدد روز وسط صفحه (بزرگتر و برجسته‌تر)
            Text(
                text = dateInfo.dayOfMonth.toPersianNumber(),
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            // ماه و سال شمسی - بالا سمت چپ
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${dateInfo.shamsiMonth} ${dateInfo.shamsiYear.toPersianNumber()}",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Right,
                    letterSpacing = 0.3.sp
                )
            }

            // تاریخ میلادی - پایین سمت چپ
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = dateInfo.miladiFullDate.toPersianNumber(),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Right
                )
            }

            // تاریخ قمری - پایین سمت راست
            Text(
                text = dateInfo.ghamari.toPersianNumber(),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.BottomEnd),
                textAlign = TextAlign.Right
            )
        }
    }
}

// ===== دیتاکلاس و توابع کمکی (بدون تغییر) =====
data class DateInfo(
    val dayOfWeek: String,
    val dayOfMonth: String,
    val shamsiMonth: String,
    val shamsiYear: String,
    val miladiFullDate: String,
    val ghamari: String,
    val time: String,
    val temperature: Int = 24
)

fun getDateInfo(): DateInfo {
    val now = Calendar.getInstance()
    val jalali = JalaliCalendar(now.time)

    val dayOfWeekFa = getPersianDayOfWeek(now)
    val shamsiMonth = jalali.monthString
    val shamsiYear = jalali.year.toString()
    val dayOfMonth = jalali.day.toString()

    val miladiDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("en"))
    val miladiFullDate = miladiDateFormat.format(now.time)

    val ummalqura = UmmalquraCalendar()
    val ghamariDay = ummalqura.get(Calendar.DAY_OF_MONTH)
    val ghamariMonth = getArabicMonthName(ummalqura)
    val ghamariYear = ummalqura.get(Calendar.YEAR)
    val ghamari = "$ghamariDay $ghamariMonth $ghamariYear"

    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)

    return DateInfo(
        dayOfWeek = dayOfWeekFa,
        dayOfMonth = dayOfMonth,
        shamsiMonth = shamsiMonth,
        shamsiYear = shamsiYear,
        miladiFullDate = miladiFullDate,
        ghamari = ghamari,
        time = time
    )
}

fun getArabicMonthName(calendar: UmmalquraCalendar): String {
    val months = arrayOf(
        "محرم", "صفر", "ربیع‌الاول", "ربیع‌الثانی", "جمادی‌الاول", "جمادی‌الثانی",
        "رجب", "شعبان", "رمضان", "شوال", "ذی‌القعده", "ذی‌الحجه"
    )
    return months[calendar.get(Calendar.MONTH)]
}

fun getPersianDayOfWeek(calendar: Calendar): String {
    val dayOfWeekIndex = calendar.get(Calendar.DAY_OF_WEEK)
    val persianDays = listOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    )
    return persianDays[(dayOfWeekIndex + 5) % 7]
}

fun String.toPersianNumber(): String {
    val englishDigits = "0123456789"
    val persianDigits = "۰۱۲۳۴۵۶۷۸۹"
    var result = this
    for (i in englishDigits.indices) {
        result = result.replace(englishDigits[i], persianDigits[i])
    }
    return result
}

fun Int.toPersianNumber(): String = this.toString().toPersianNumber()