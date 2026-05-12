package com.shams.notetodo.screens

import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.shams.notetodo.model.Task
import com.shams.notetodo.model.TaskCategory
import com.shams.notetodo.ui.components.ShamsiCalendarScreen
import com.shams.notetodo.ui.components.ShamsiDate
import com.shams.notetodo.vieewmodel.TaskViewModel
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
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 750.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AddTaskBottomSheetContent(
                viewModel = viewModel,
                onDismiss = onDismiss,
                modifier = Modifier
                    .imePadding()
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTaskBottomSheetContent(
    viewModel: TaskViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(TaskCategory.PERSONAL) }
    var selectedDate by remember { mutableStateOf<ShamsiDate?>(null) }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance()) }

    val formattedTime by remember(selectedTime) {
        derivedStateOf {
            val hour = selectedTime.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
            val minute = selectedTime.get(Calendar.MINUTE).toString().padStart(2, '0')
            "$hour:$minute"
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "افزودن تسک جدید",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("عنوان تسک", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(
            text = "دسته‌بندی:",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TaskCategory.values().forEach { item ->
                val isSelected = category == item
                FilterChip(
                    selected = isSelected,
                    onClick = { category = item },
                    label = { Text(text = item.toPersianName(), color = if (isSelected) Color.White else Color(0xFF2196F3)) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2196F3),
                        containerColor = Color(0xFFE3F2FD),
                        selectedLabelColor = Color.White,
                        labelColor = Color(0xFF2196F3)
                    ),
                    modifier = Modifier.height(36.dp)
                )
            }
        }

        Text(
            text = "تاریخ مورد نظر را از تقویم انتخاب کنید:",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )

        Box(modifier = Modifier.heightIn(max = 280.dp)) {
            ShamsiCalendarScreen { date -> selectedDate = date }
        }

        OutlinedButton(
            onClick = { showTimePicker(context) { selectedTime = it } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⏰ ساعت انتخاب شده: $formattedTime")
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && selectedDate != null) {
                    val dateTimeString = "${selectedDate.toString()} $formattedTime"

            // تبدیل تاریخ شمسی به میلادی و ساخت Calendar برای آلارم
            val gregorianDate = JalaliCalendar(
                selectedDate!!.year,
                selectedDate!!.month,
                selectedDate!!.day
            ).toGregorian()

                    val alarmCal = Calendar.getInstance().apply {
                        time = Date.from(gregorianDate.toInstant())

                        set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))

                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

            viewModel.addTask(
                    context = context,
                    task = Task(
                        title = title,
                        category = category,
                        description = "",
                        isDone = false,
                        dateTime = dateTimeString
                    ),
                    alarmCalendar = alarmCal
                )
                onDismiss()

                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && selectedDate != null
        ) {
            Text("💾 ذخیره")
        }
    }
}

fun showTimePicker(context: Context, onTimeSelected: (Calendar) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    TimePickerDialog(context, { _, selectedHour, selectedMinute ->
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinute)
        onTimeSelected(calendar)
    }, hour, minute, true).show()
}

// تابع کمکی برای نام فارسی دسته‌بندی‌ها
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


