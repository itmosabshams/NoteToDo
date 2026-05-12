package com.shams.notetodo.shamsicalendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shams.notetodo.ui.components.ShamsiDate
import ir.huri.jcal.JalaliCalendar
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShamsiCalendarScreen(
    onDateSelected: (ShamsiDate) -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val scope = rememberCoroutineScope()

        val today = JalaliCalendar().let { ShamsiDate(it.year, it.month, it.day) }

        var currentYear by remember { mutableStateOf(JalaliCalendar().year) }
        var currentMonth by remember { mutableStateOf(JalaliCalendar().month) }

        val pagerState = rememberPagerState(
            initialPage = currentMonth - 1,
            pageCount = { 12 }
        )

        LaunchedEffect(pagerState.currentPage) {
            val newMonth = pagerState.currentPage + 1
            if (newMonth != currentMonth) {
                if (newMonth == 1 && currentMonth == 12) {
                    currentYear++
                } else if (newMonth == 12 && currentMonth == 1) {
                    currentYear--
                }
                currentMonth = newMonth
            }
        }

        var selectedDate by remember { mutableStateOf<ShamsiDate?>(null) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // ==================== هدر ماه ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "›",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3),
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            scope.launch {
                                if (currentMonth > 1) {
                                    pagerState.animateScrollToPage(currentMonth - 2)
                                } else {
                                    pagerState.animateScrollToPage(11)
                                }
                            }
                        }
                        .padding(horizontal = 12.dp)
                )

                Text(
                    text = getPersianMonthName(currentMonth),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "‹",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3),
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            scope.launch {
                                if (currentMonth < 12) {
                                    pagerState.animateScrollToPage(currentMonth)
                                } else {
                                    pagerState.animateScrollToPage(0)
                                }
                            }
                        }
                        .padding(horizontal = 12.dp)
                )
            }

            // ==================== روزهای هفته ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEachIndexed { index, day ->
                    Text(
                        text = day,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (index == 6) Color(0xFFE53935) else Color(0xFF757575),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                color = Color(0xFFEEEEEE),
                thickness = 1.dp
            )

            // ==================== تقویم ====================
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                userScrollEnabled = true
            ) { page ->
                val month = page + 1
                val year = if (month == 1 && currentMonth == 12 && page < pagerState.currentPage) {
                    currentYear + 1
                } else if (month == 12 && currentMonth == 1 && page > pagerState.currentPage) {
                    currentYear - 1
                } else {
                    currentYear
                }

                CalendarGrid(
                    year = year,
                    month = month,
                    today = today,
                    selectedDate = selectedDate,
                    onDateSelected = { date ->
                        selectedDate = date
                        onDateSelected(date)
                    }
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    year: Int,
    month: Int,
    today: ShamsiDate,
    selectedDate: ShamsiDate?,
    onDateSelected: (ShamsiDate) -> Unit
) {
    val daysInMonth = daysInShamsiMonth(year, month)
    val firstDayIndex = firstWeekDayOfMonth(year, month)
    val totalCells = daysInMonth + firstDayIndex
    val rows = (totalCells + 6) / 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..6) {
                    val index = row * 7 + col
                    if (index < firstDayIndex || index >= totalCells) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val day = index - firstDayIndex + 1
                        val date = ShamsiDate(year, month, day)
                        val isToday = date == today
                        val isSelected = date == selectedDate
                        val isFriday = col == 6

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(2.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    onDateSelected(date)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isSelected -> {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                color = Color(0xFF2196F3),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                isToday -> {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                color = Color(0xFFE3F2FD),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2196F3)
                                        )
                                    }
                                }
                                else -> {
                                    Text(
                                        text = day.toString(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isFriday) Color(0xFFE53935) else Color(0xFF424242)
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

fun getPersianMonthName(month: Int): String {
    return listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    ).getOrElse(month - 1) { "نامشخص" }
}

fun daysInShamsiMonth(year: Int, month: Int): Int = when (month) {
    in 1..6 -> 31
    in 7..11 -> 30
    12 -> if (isShamsiLeapYear(year)) 30 else 29
    else -> 30
}

fun isShamsiLeapYear(year: Int): Boolean {
    val breaks = intArrayOf(-61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210, 1635, 2060, 2097,
        2192, 2262, 2324, 2394, 2456, 3178)
    var leap = -14
    var jp = breaks[0]
    var i = 1
    while (i < breaks.size && year >= breaks[i]) {
        val jump = breaks[i] - jp
        leap += jump / 33 * 8 + (jump % 33) / 4
        jp = breaks[i]
        i++
    }
    val nYears = year - jp
    leap += nYears / 33 * 8 + ((nYears % 33) + 3) / 4
    val mod = (leap + 1) % 33
    return mod in listOf(1, 5, 9, 13, 17, 22, 26, 30)
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