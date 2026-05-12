package com.shams.notetodo.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs

@Composable
fun CustomTimePicker(
    initialHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    initialMinute: Int = Calendar.getInstance().get(Calendar.MINUTE),
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    // مرکز محدوده وسط (Y)
    var centerY by remember { mutableStateOf(0f) }

    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = 1000 + initialHour)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = 1000 + initialMinute)
    val coroutineScope = rememberCoroutineScope()

    // تشخیص عدد وسط برای ساعت
    LaunchedEffect(hourListState.firstVisibleItemIndex) {
        val layoutInfo = hourListState.layoutInfo
        val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2

        var closestIndex = -1
        var minDistance = Int.MAX_VALUE

        layoutInfo.visibleItemsInfo.forEach { itemInfo ->
            val itemCenter = itemInfo.offset + itemInfo.size / 2
            val distance = abs(itemCenter - viewportCenter)
            if (distance < minDistance) {
                minDistance = distance
                closestIndex = itemInfo.index
            }
        }

        if (closestIndex != -1) {
            val hour = closestIndex % 24
            if (selectedHour != hour) {
                selectedHour = hour
            }
        }
    }

    // تشخیص عدد وسط برای دقیقه
    LaunchedEffect(minuteListState.firstVisibleItemIndex) {
        val layoutInfo = minuteListState.layoutInfo
        val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2

        var closestIndex = -1
        var minDistance = Int.MAX_VALUE

        layoutInfo.visibleItemsInfo.forEach { itemInfo ->
            val itemCenter = itemInfo.offset + itemInfo.size / 2
            val distance = abs(itemCenter - viewportCenter)
            if (distance < minDistance) {
                minDistance = distance
                closestIndex = itemInfo.index
            }
        }

        if (closestIndex != -1) {
            val minute = closestIndex % 60
            if (selectedMinute != minute) {
                selectedMinute = minute
            }
        }
    }

    // گرفتن مرکز محدوده برای سایز اعداد
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                centerY = coordinates.size.height / 2f
            }
    ) {
        // این Box فقط برای گرفتن مرکز استفاده شده
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // هدر
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "انتخاب زمان",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color(0xFF757575))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ==================== سلکتور ساعت و دقیقه ====================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // سلکتور دقیقه (چپ)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        LazyColumn(
                            state = minuteListState,
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(2000) { index ->
                                val minute = index % 60

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            coroutineScope.launch {
                                                minuteListState.animateScrollToItem(index)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // تشخیص نزدیکی به مرکز
                                    var isNearCenter by remember { mutableStateOf(false) }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned { coordinates ->
                                                val itemPosition = coordinates.positionInParent()
                                                val itemCenterY = itemPosition.y + coordinates.size.height / 2
                                                val distance = abs(itemCenterY - (220f / 2)) // 220 ارتفاع کل
                                                isNearCenter = distance < 30f
                                            }
                                    ) {
                                        Text(
                                            text = minute.toString().padStart(2, '0'),
                                            fontSize = if (isNearCenter) 34.sp else 20.sp,
                                            fontWeight = if (isNearCenter) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isNearCenter) Color(0xFF2196F3) else Color(0xFF9E9E9E),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // جداکننده :
                    Text(
                        text = ":",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // سلکتور ساعت (راست)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        LazyColumn(
                            state = hourListState,
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(2000) { index ->
                                val hour = index % 24
                                val displayHour = hour.toString().padStart(2, '0')

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            coroutineScope.launch {
                                                hourListState.animateScrollToItem(index)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    var isNearCenter by remember { mutableStateOf(false) }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned { coordinates ->
                                                val itemPosition = coordinates.positionInParent()
                                                val itemCenterY = itemPosition.y + coordinates.size.height / 2
                                                val distance = abs(itemCenterY - (220f / 2))
                                                isNearCenter = distance < 30f
                                            }
                                    ) {
                                        Text(
                                            text = displayHour,
                                            fontSize = if (isNearCenter) 34.sp else 20.sp,
                                            fontWeight = if (isNearCenter) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isNearCenter) Color(0xFF2196F3) else Color(0xFF9E9E9E),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // نمایش AM/PM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val amPm = if (selectedHour >= 12) "PM" else "AM"
                    Surface(
                        shape = RoundedCornerShape(30.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = amPm,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.padding(horizontal = 30.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // نمایش زمان انتخاب شده
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color(0xFFF5F5F5)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "زمان انتخاب شده:",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // دکمه تایید
                Button(
                    onClick = {
                        onTimeSelected(selectedHour, selectedMinute)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تأیید", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}