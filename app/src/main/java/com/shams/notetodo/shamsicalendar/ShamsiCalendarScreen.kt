package com.shams.notetodo.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import ir.huri.jcal.JalaliCalendar
import java.util.Calendar
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShamsiCalendarScreen(
    onDateSelected: (ShamsiDate) -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

        var currentYear by remember { mutableStateOf(JalaliCalendar().year) }
        var currentMonth by remember { mutableStateOf(JalaliCalendar().month) }
        var selectedDate by remember { mutableStateOf<ShamsiDate?>(null) }
        val today = JalaliCalendar().let { ShamsiDate(it.year, it.month, it.day) }

        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "<",
                    modifier = Modifier.clickable {
                        if (currentMonth == 1) {
                            currentMonth = 12
                            currentYear -= 1
                        } else currentMonth -= 1
                    },
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "${getPersianMonthName(currentMonth)} ${currentYear.toPersianDigits()}",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = ">",
                    modifier = Modifier.clickable {
                        if (currentMonth == 12) {
                            currentMonth = 1
                            currentYear += 1
                        } else currentMonth += 1
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // روزهای هفته
            val weekDays = listOf("شنبه", "یکشنبه", "دوشنبه", "سه شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Divider(modifier = Modifier.fillMaxWidth().background(Color.Gray))
            Spacer(modifier = Modifier.height(4.dp))

            // بدنه ماه
            val daysInMonth = daysInShamsiMonth(currentYear, currentMonth)
            val firstDayIndex = firstWeekDayOfMonth(currentYear, currentMonth)
            val totalCells = daysInMonth + firstDayIndex
            val rows = (totalCells + 6) / 7

            Column {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val index = row * 7 + col
                            if (index < firstDayIndex || index >= totalCells) {
                                Spacer(modifier = Modifier.weight(1f).height(36.dp))
                            } else {
                                val day = index - firstDayIndex + 1
                                val date = ShamsiDate(currentYear, currentMonth, day)
                                val isToday = date == today
                                val isSelected = date == selectedDate

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .padding(2.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            selectedDate = date
                                            onDateSelected(date)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(
                                                color = if (isSelected) Color(0xFF2196F3) else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.toPersianDigits(),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = if (isSelected) Color.White else if (isToday) Color(0xFF2196F3) else Color.Black,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// تبدیل اعداد به فارسی
fun Int.toPersianDigits(): String {
    val persianDigits = listOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')
    return this.toString().map { it.digitToInt().let { persianDigits[it] } }.joinToString("")
}

fun getPersianMonthName(month: Int): String {
    return listOf(
        "فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور",
        "مهر","آبان","آذر","دی","بهمن","اسفند"
    ).getOrElse(month-1) { "نامشخص" }
}

fun daysInShamsiMonth(year: Int, month: Int): Int = when (month) {
    in 1..6 -> 31
    in 7..11 -> 30
    12 -> if (isShamsiLeapYear(year)) 30 else 29
    else -> 30
}

fun isShamsiLeapYear(year: Int): Boolean {
    val breaks = intArrayOf(-61,9,38,199,426,686,756,818,1111,1181,1210,1635,2060,2097,
        2192,2262,2324,2394,2456,3178)
    var leap = -14
    var jp = breaks[0]
    var i = 1
    while (i < breaks.size && year >= breaks[i]) {
        val jump = breaks[i] - jp
        leap += jump / 33 * 8 + (jump % 33) /4
        jp = breaks[i]
        i++
    }
    val nYears = year - jp
    leap += nYears / 33 *8 + ((nYears %33)+3)/4
    val mod = (leap +1)%33
    return mod in listOf(1,5,9,13,17,22,26,30)
}

@RequiresApi(Build.VERSION_CODES.O)
fun firstWeekDayOfMonth(year: Int, month: Int): Int {
    val gregorian = JalaliCalendar(year, month, 1).toGregorian()
    val cal = Calendar.getInstance()
    cal.time = Date.from(gregorian.toInstant())
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SATURDAY -> 0
        Calendar.SUNDAY -> 1
        Calendar.MONDAY -> 2
        Calendar.TUESDAY -> 3
        Calendar.WEDNESDAY -> 4
        Calendar.THURSDAY -> 5
        Calendar.FRIDAY -> 6
        else -> 0
    }


}



