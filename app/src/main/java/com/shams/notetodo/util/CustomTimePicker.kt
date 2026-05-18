package com.shams.notetodo.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

private const val ITEM_HEIGHT = 52
private const val VISIBLE_ITEMS = 5
private const val LIST_SIZE = 10_000
private const val CENTER_INDEX = LIST_SIZE / 2

@Composable
fun CustomTimePicker(
    initialHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    initialMinute: Int = Calendar.getInstance().get(Calendar.MINUTE),
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    android.util.Log.d("TaskItemDebuggggg 222", "=========================================================")
    android.util.Log.d("TaskItemDebuggggg 222", "initialHour='$initialHour', initialHour='$initialMinute'")

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    val coroutineScope = rememberCoroutineScope()

    // قرار دادن مقدار اولیه در مرکز لیست
    val initialHourIndex = remember(initialHour) {
        CENTER_INDEX - (CENTER_INDEX % 24) + initialHour
    }

    val initialMinuteIndex = remember(initialMinute) {
        CENTER_INDEX - (CENTER_INDEX % 60) + initialMinute
    }

    val hourListState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialHourIndex
    )

    val minuteListState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialMinuteIndex
    )

    // تشخیص آیتم انتخاب شده در وسط
    LaunchedEffect(hourListState) {
        snapshotFlow {
            hourListState.firstVisibleItemIndex to
                    hourListState.firstVisibleItemScrollOffset
        }
            .map { (index, offset) ->
                val selectedIndex =
                    if (offset > ITEM_HEIGHT / 2) index + 1 else index
                selectedIndex % 24
            }
            .distinctUntilChanged()
            .collectLatest {
                selectedHour = it
            }
    }

    LaunchedEffect(minuteListState) {
        snapshotFlow {
            minuteListState.firstVisibleItemIndex to
                    minuteListState.firstVisibleItemScrollOffset
        }
            .map { (index, offset) ->
                val selectedIndex =
                    if (offset > ITEM_HEIGHT / 2) index + 1 else index
                selectedIndex % 60
            }
            .distinctUntilChanged()
            .collectLatest {
                selectedMinute = it
            }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Header
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
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = Color(0xFF757575)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Time Picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((ITEM_HEIGHT * VISIBLE_ITEMS).dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Minute Picker
                    TimeColumn(
                        maxValue = 60,
                        selectedValue = selectedMinute,
                        listState = minuteListState,
                        modifier = Modifier.weight(1f),
                        onItemClick = { index ->
                            coroutineScope.launch {
                                minuteListState.animateScrollToItem(index)
                            }
                        }
                    )

                    Text(
                        text = ":",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Hour Picker
                    TimeColumn(
                        maxValue = 24,
                        selectedValue = selectedHour,
                        listState = hourListState,
                        modifier = Modifier.weight(1f),
                        onItemClick = { index ->
                            coroutineScope.launch {
                                hourListState.animateScrollToItem(index)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // نمایش زمان انتخاب‌شده
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
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
                            text = "%02d:%02d".format(
                                selectedHour,
                                selectedMinute
                            ),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm Button
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
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "تأیید",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeColumn(
    maxValue: Int,
    selectedValue: Int,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = ITEM_HEIGHT.dp
    val visibleHeight = (ITEM_HEIGHT * VISIBLE_ITEMS).dp

    Box(
        modifier = modifier
            .height(visibleHeight)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {

        // کادر آبی وسط
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(itemHeight)
                .background(
                    Color(0xFFE3F2FD),
                    RoundedCornerShape(16.dp)
                )
                .align(Alignment.Center)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                vertical = itemHeight * ((VISIBLE_ITEMS - 1) / 2)
            )
        ) {
            items(LIST_SIZE) { index ->
                val value = index % maxValue
                val isSelected = value == selectedValue

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable(
                            indication = null,
                            interactionSource = remember {
                                MutableInteractionSource()
                            }
                        ) {
                            onItemClick(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString().padStart(2, '0'),
                        fontSize = if (isSelected) 34.sp else 20.sp,
                        fontWeight = if (isSelected)
                            FontWeight.Bold
                        else
                            FontWeight.Medium,
                        color = if (isSelected)
                            Color(0xFF2196F3)
                        else
                            Color(0xFF9E9E9E),
                        maxLines = 1
                    )
                }
            }
        }
    }
}