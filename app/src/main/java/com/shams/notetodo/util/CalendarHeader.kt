package com.shams.notetodo.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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

    val mainColor = Color(0xFF03A9F4)  // آبی روشن
    val waveColor = Color(0xFF0288D1)  // آبی تیره

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            drawRect(color = mainColor, size = Size(width, height))

            val wavePath = Path().apply {
                moveTo(0f, height * 0.75f)
                quadraticBezierTo(width / 2, height, width, height * 0.75f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(path = wavePath, color = waveColor)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // روز هفته - بالا سمت راست3
            Text(
                text = dateInfo.dayOfWeek,
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.TopStart),
                textAlign = TextAlign.Right
            )


            // عدد روز وسط صفحه
            Text(
                text = dateInfo.dayOfMonth.toPersianNumber(),
                color = Color.White,
                fontSize = 60.sp,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )


            // ماه و سال شمسی - پایین سمت چپ
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${dateInfo.shamsiMonth} ${dateInfo.shamsiYear.toPersianNumber()}",
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right
                )

            }
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = dateInfo.miladiFullDate.toPersianNumber(),
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Right
                )
            }
            // تاریخ قمری - پایین سمت راست
            Text(
                text = dateInfo.ghamari.toPersianNumber(),
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomEnd),
                textAlign = TextAlign.Right
            )
        }
    }
}

// ===== دیتاکلاس و توابع کمکی =====
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

// ==== تبدیل اعداد به فارسی ====
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
