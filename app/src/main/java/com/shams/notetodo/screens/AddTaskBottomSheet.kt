package com.shams.notetodo.screens

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shams.notetodo.model.Task
import com.shams.notetodo.model.TaskCategory
import com.shams.notetodo.shamsicalendar.ShamsiCalendarScreen
import com.shams.notetodo.ui.components.ShamsiDate
import com.shams.notetodo.utils.CustomTimePicker
import com.shams.notetodo.utils.PermissionHelper
import com.shams.notetodo.viewmodel.TaskViewModel
import java.util.Calendar
import ir.huri.jcal.JalaliCalendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTaskBottomSheet(
    viewModel: TaskViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE0E0E0))
            )
        },
        modifier = Modifier.fillMaxHeight()
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AddTaskBottomSheetContent(
                viewModel = viewModel,
                onDismiss = onDismiss
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTaskBottomSheetContent(
    viewModel: TaskViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(TaskCategory.PERSONAL) }
    var selectedDate by remember { mutableStateOf<ShamsiDate?>(null) }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showCustomTimePicker by remember { mutableStateOf(false) }

    val formattedTime by remember(selectedTime) {
        derivedStateOf {
            val hour = selectedTime.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
            val minute = selectedTime.get(Calendar.MINUTE).toString().padStart(2, '0')
            "$hour:$minute"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // ==================== هدر ====================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ایجاد تسک جدید",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
                Text(
                    text = "اطلاعات تسک را وارد کنید",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color(0xFF757575))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==================== فیلد عنوان ====================
        OutlinedTextField(
            value = title,
            onValueChange = { title = it; showError = false },
            label = { Text("عنوان تسک") },
            placeholder = { Text("مثال: خرید شیر") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            isError = showError && title.isBlank(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2196F3),
                unfocusedBorderColor = Color(0xFFBDBDBD),
                focusedLabelColor = Color(0xFF2196F3)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ==================== دسته‌بندی ====================
        Text(
            text = "دسته‌بندی",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskCategory.values().forEach { cat ->
                val isSelected = category == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { category = cat },
                    label = {
                        Text(
                            text = cat.toPersianName(),
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else Color(0xFF1976D2)
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2196F3),
                        containerColor = Color(0xFFE3F2FD),
                        selectedLabelColor = Color.White,
                        labelColor = Color(0xFF1976D2)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ==================== تقویم ====================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "انتخاب تاریخ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF424242),
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
                )
                ShamsiCalendarScreen(
                    onDateSelected = { date ->
                        selectedDate = date
                        showError = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ==================== انتخاب ساعت ====================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        tint = Color(0xFF2196F3),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ساعت یادآوری",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp)),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showCustomTimePicker = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE3F2FD))
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ==================== نمایش تاریخ انتخاب شده ====================
        if (selectedDate != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        tint = Color(0xFF2196F3),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${selectedDate!!.year}/${selectedDate!!.month}/${selectedDate!!.day} - $formattedTime",
                        color = Color(0xFF1976D2),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        tint = Color(0xFFFF9800),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "تاریخی انتخاب نشده است",
                        color = Color(0xFFE65100),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ==================== نمایش خطا ====================
        if (showError && errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, tint = Color(0xFFD32F2F), contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(errorMessage, color = Color(0xFFD32F2F), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==================== دکمه ذخیره ====================
        Button(
            onClick = {
                if (title.isBlank()) {
                    errorMessage = "لطفاً عنوان تسک را وارد کنید"
                    showError = true
                    return@Button
                }

                if (selectedDate == null) {
                    errorMessage = "لطفاً تاریخ را از تقویم انتخاب کنید"
                    showError = true
                    return@Button
                }

                if (!PermissionHelper.hasNotificationPermission(context)) {
                    errorMessage = "لطفاً مجوز اعلان را بدهید"
                    showError = true
                    if (context is androidx.activity.ComponentActivity) {
                        PermissionHelper.requestNotificationPermission(context)
                    }
                    return@Button
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!PermissionHelper.hasExactAlarmPermission(context)) {
                        errorMessage = "لطفاً مجوز تنظیم آلارم را بدهید"
                        showError = true
                        if (context is androidx.activity.ComponentActivity) {
                            PermissionHelper.requestExactAlarmPermission(context)
                        }
                        return@Button
                    }
                }

                selectedDate?.let { date ->
                    val dateTimeString = "${date.year}/${date.month}/${date.day} $formattedTime"

                    val gregorianDate = JalaliCalendar(
                        date.year,
                        date.month,
                        date.day
                    ).toGregorian()

                    val alarmCal = Calendar.getInstance().apply {
                        time = Date.from(gregorianDate.toInstant())
                        set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    if (alarmCal.timeInMillis <= System.currentTimeMillis()) {
                        errorMessage = "زمان انتخابی باید از زمان فعلی جلوتر باشد"
                        showError = true
                        return@Button
                    }

                    val task = Task(
                        title = title,
                        category = category,
                        description = "",
                        isDone = false,
                        dateTime = dateTimeString
                    )

                    viewModel.addTask(context, task, alarmCal)
                    onDismiss()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ذخیره تسک", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    // ==================== TimePicker سفارشی دایره‌ای ====================
    if (showCustomTimePicker) {
        CustomTimePicker(
            initialHour = selectedTime.get(Calendar.HOUR_OF_DAY),
            initialMinute = selectedTime.get(Calendar.MINUTE),
            onTimeSelected = { hour, minute ->
                selectedTime.set(Calendar.HOUR_OF_DAY, hour)
                selectedTime.set(Calendar.MINUTE, minute)
                showCustomTimePicker = false
            },
            onDismiss = { showCustomTimePicker = false }
        )
    }
}

fun TaskCategory.toPersianName(): String = when (this) {
    TaskCategory.ALL -> "همه"
    TaskCategory.PERSONAL -> "شخصی"
    TaskCategory.BUY -> "خرید"
    TaskCategory.DAILY -> "روزانه"
    TaskCategory.COSTS -> "هزینه‌ها"
    TaskCategory.INSTALLMENTS -> "اقساط"
    TaskCategory.MEETING -> "جلسه"
    TaskCategory.SPORT -> "ورزش"
}