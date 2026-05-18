package com.shams.notetodo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shams.notetodo.model.Task
import com.shams.notetodo.model.TaskCategory
import com.shams.notetodo.util.toPersianName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TaskItem(
    task: Task,
    onToggle: (Task) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    var showAnimation by remember { mutableStateOf(false) }
    var showChecked by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val shakeOffset = remember { Animatable(0f) }
    val cardElevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
    )
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val coroutineScope = rememberCoroutineScope()

    // تابع تبدیل اعداد به فارسی با پشتیبانی از ساعت
    fun String.toPersianDigits(): String {
        if (isEmpty()) return this

        val persianDigits = mapOf(
            '0' to '۰', '1' to '۱', '2' to '۲', '3' to '۳', '4' to '۴',
            '5' to '۵', '6' to '۶', '7' to '۷', '8' to '۸', '9' to '۹'
        )

        return this.map { char ->
            persianDigits[char] ?: char
        }.joinToString("")
    }

    fun formatDateTime(dateTimeStr: String): Pair<String, String> {
        return try {
            if (dateTimeStr.isBlank() || dateTimeStr == "-") {
                return Pair("-", "-")
            }

            val parts = dateTimeStr.trim().split(" ")

            if (parts.size < 2) {
                return Pair(parts[0].toPersianDigits(), "-")
            }

            val date = parts[0].toPersianDigits()
            var time = parts[1]

            // اطمینان از فرمت HH:MM
            if (time.contains(":")) {
                val timeParts = time.split(":")
                if (timeParts.size >= 2) {
                    val hour = timeParts[0].padStart(2, '0').take(2)
                    val minute = timeParts[1].padStart(2, '0').take(2)
                    time = "$hour:$minute"
                }
            }

            Pair(date, time.toPersianDigits())
        } catch (e: Exception) {
            Pair(dateTimeStr.toPersianDigits(), "-")
        }
    }
    android.util.Log.d("AlarmDebug", "task.dateTime from DB: '${task.dateTime}'")
    val (persianDate, persianTime) = formatDateTime(task.dateTime)
    android.util.Log.d("AlarmDebug", "After format: date='$persianDate', time='$persianTime'")


    LaunchedEffect(showAnimation) {
        if (showAnimation) {
            coroutineScope.launch {
                repeat(3) {
                    shakeOffset.animateTo(-5f, tween(30))
                    shakeOffset.animateTo(5f, tween(30))
                }
                shakeOffset.animateTo(0f, tween(40))
            }
            delay(1000)
            onToggle(task)
            delay(500)
            showAnimation = false
            showChecked = false
        }
    }

    // رنگ‌های مدرن و حرفه‌ای
    val accentColor = Color(0xFF6366F1)
    val categoryColor = getCategoryColor(task.category)
    val categoryLightColor = categoryColor.copy(alpha = 0.12f)
    val categoryGradient = Brush.linearGradient(
        colors = listOf(categoryColor, categoryColor.copy(alpha = 0.85f))
    )

    // گرادینت نوار پایین
    val bottomBarGradient = Brush.horizontalGradient(
        colors = listOf(
            if (task.isDone) Color(0xFFCBD5E1) else categoryColor,
            if (task.isDone) Color(0xFF94A3B8) else categoryColor.copy(alpha = 0.7f),
            if (task.isDone) Color(0xFFCBD5E1) else categoryColor
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(108.dp)
            .graphicsLayer {
                translationX = shakeOffset.value
                scaleX = cardScale
                scaleY = cardScale
            }
            .shadow(
                elevation = cardElevation,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (task.isDone) Color.Transparent else Color.Black.copy(alpha = 0.06f),
                ambientColor = Color.Transparent
            )
            .clickable(
                enabled = onClick != null,
                onClick = { onClick?.invoke() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (task.isDone && !showAnimation) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFAFAFA),
                                Color(0xFFF5F5F5)
                            )
                        )
                    } else Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFFEFEFE))
                    )
                )
        ) {
            if (showAnimation) {
                CelebrationContent(accentColor = accentColor)
            } else {
                TaskContent(
                    task = task,
                    persianDate = persianDate,
                    persianTime = persianTime,
                    categoryColor = categoryColor,
                    categoryLightColor = categoryLightColor,
                    categoryGradient = categoryGradient,
                    accentColor = accentColor,
                    showChecked = showChecked,
                    onToggle = {
                        if (!task.isDone) {
                            showChecked = true
                            coroutineScope.launch {
                                delay(150)
                                showAnimation = true
                            }
                        } else {
                            onToggle(task)
                        }
                    }
                )
            }

            // نوار باریک پایین کارت
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(bottomBarGradient)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun CelebrationContent(accentColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.05f),
                        Color.White
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var dotCount by remember { mutableStateOf(0) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(200)
                dotCount = (dotCount + 1) % 4
            }
        }

        Text(
            text = "🎉" + ".".repeat(dotCount),
            fontSize = 44.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "آفرین!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}

@Composable
private fun TaskContent(
    task: Task,
    persianDate: String,
    persianTime: String,
    categoryColor: Color,
    categoryLightColor: Color,
    categoryGradient: Brush,
    accentColor: Color,
    showChecked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // آیکون دسته‌بندی
        CategoryIcon(
            category = task.category,
            gradient = categoryGradient,
            isDone = task.isDone
        )

        Spacer(modifier = Modifier.width(12.dp))

        // اطلاعات اصلی
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // عنوان تسک
            Text(
                text = task.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (task.isDone) Color(0xFF94A3B8) else Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                letterSpacing = 0.3.sp
            )

            // دسته‌بندی و اطلاعات زمان
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // برچسب دسته‌بندی
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = categoryLightColor,
                    modifier = Modifier
                        .height(24.dp)
                        .wrapContentWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getCategoryIcon(task.category),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = task.category.toPersianName(),
                            fontSize = 12.sp,
                            color = categoryColor,
                            fontWeight = FontWeight.SemiBold

                        )
                    }
                }

                // نشانگر زمان - نمایش دقیق
                if (persianTime != "-") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "⏰",
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$persianDate | $persianTime",
                            fontSize = 12.sp,
                            color = if (task.isDone) Color(0xFFCBD5E1) else Color(0xFF64748B),
                            fontWeight = FontWeight.Medium

                        )
                        android.util.Log.d("TaskItemDebuggggg", "persianDate='$persianDate', persianTime='$persianTime'")
                    }
                } else {
                    // فقط تاریخ
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "📅",
                            fontSize = 12.sp
                        )
                        Text(
                            text = persianDate,
                            fontSize = 12.sp,
                            color = if (task.isDone) Color(0xFFCBD5E1) else Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // دکمه چک باکس مدرن
        ModernCheckbox(
            checked = showChecked || task.isDone,
            onToggle = onToggle,
            accentColor = accentColor,
            isDone = task.isDone
        )
    }
}

@Composable
private fun CategoryIcon(
    category: TaskCategory,
    gradient: Brush,
    isDone: Boolean
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                if (isDone) Brush.horizontalGradient(
                    colors = listOf(Color(0xFFCBD5E1), Color(0xFFE2E8F0))
                ) else gradient
            )
            .shadow(
                elevation = 3.dp,
                shape = CircleShape,
                spotColor = if (!isDone) getCategoryColor(category).copy(alpha = 0.25f) else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getCategoryIcon(category),
            fontSize = 24.sp
        )
    }
}

@Composable
private fun ModernCheckbox(
    checked: Boolean,
    onToggle: () -> Unit,
    accentColor: Color,
    isDone: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) accentColor else Color.White,
        animationSpec = tween(durationMillis = 200),
        label = "bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) accentColor else Color(0xFFE2E8F0),
        animationSpec = tween(durationMillis = 200),
        label = "border"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(27.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Text(
                text = if (isDone) "🔒" else "✓",
                fontSize = 16.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = checkScale
                    scaleY = checkScale
                }
            )
        }
    }
}

private fun getCategoryColor(category: TaskCategory): Color {
    return when (category) {
        TaskCategory.ALL -> Color(0xFF6366F1)
        TaskCategory.PERSONAL -> Color(0xFF10B981)
        TaskCategory.BUY -> Color(0xFFF59E0B)
        TaskCategory.DAILY -> Color(0xFF06B6D4)
        TaskCategory.COSTS -> Color(0xFFEF4444)
        TaskCategory.INSTALLMENTS -> Color(0xFF8B5CF6)
        TaskCategory.MEETING -> Color(0xFF3B82F6)
        TaskCategory.SPORT -> Color(0xFF22C55E)
    }
}

private fun getCategoryIcon(category: TaskCategory): String {
    return when (category) {
        TaskCategory.ALL -> "📋"
        TaskCategory.PERSONAL -> "👤"
        TaskCategory.BUY -> "🛒"
        TaskCategory.DAILY -> "📅"
        TaskCategory.COSTS -> "💰"
        TaskCategory.INSTALLMENTS -> "📊"
        TaskCategory.MEETING -> "👥"
        TaskCategory.SPORT -> "⚽"
    }
}