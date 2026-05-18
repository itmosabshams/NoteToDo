package com.shams.notetodo.alarm

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RingingScreen(
    taskTitle: String,
    taskDescription: String = "",
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showSnoozeDialog by remember { mutableStateOf(false) }

    // انیمیشن‌ها
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        ),
                        center = Offset(0.5f, 0.4f),
                        radius = 1.5f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // ساعت و تاریخ
                CurrentTimeDisplay(alpha = alpha)

                Spacer(modifier = Modifier.weight(1f))

                // آیکون زنگ متحرک
                AnimatedAlarmIcon(
                    pulseScale = pulseScale,
                    rotationAngle = rotationAngle,
                    alpha = alpha
                )

                Spacer(modifier = Modifier.height(32.dp))

                // عنوان تسک
                TaskTitleSection(
                    title = taskTitle,
                    description = taskDescription
                )

                Spacer(modifier = Modifier.weight(1f))

                // دکمه‌های اکشن
                ActionButtons(
                    onDismiss = onDismiss,
                    onSnooze = { showSnoozeDialog = true }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // دیالوگ انتخاب زمان اسنوز
    if (showSnoozeDialog) {
        SnoozeOptionsDialog(
            onSnoozeSelected = { minutes ->
                showSnoozeDialog = false
                // فراخوانی متد snoozeAlarm از Activity
                if (context is RingingActivity) {
                    context.snoozeAlarm(minutes)
                }
            },
            onDismiss = { showSnoozeDialog = false }
        )
    }
}

@Composable
private fun CurrentTimeDisplay(alpha: Float) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val calendar = java.util.Calendar.getInstance()
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
            val minute = calendar.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')
            currentTime = "$hour:$minute"

            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            currentDate = getPersianMonthName(month)
            currentDate = "$day $currentDate $year"

            delay(1000)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = currentTime,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = alpha),
            letterSpacing = 4.sp
        )
        Text(
            text = currentDate,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = alpha * 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AnimatedAlarmIcon(pulseScale: Float, rotationAngle: Float, alpha: Float) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        // رینگ‌های متحرک
        val ringScales = remember { List(3) { Animatable(1f) } }

        LaunchedEffect(Unit) {
            ringScales.forEachIndexed { index, scale ->
                launch {
                    delay(index * 200L)
                    while (true) {
                        scale.animateTo(
                            targetValue = 2.2f,
                            animationSpec = tween(1500, easing = FastOutSlowInEasing)
                        )
                        scale.snapTo(1f)
                        delay(500)
                    }
                }
            }
        }

        ringScales.forEachIndexed { index, scale ->
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale.value)
                    .border(
                        width = 2.dp,
                        color = Color(0xFF2196F3).copy(alpha = alpha * (1f - scale.value / 2.2f)),
                        shape = CircleShape
                    )
            )
        }

        // آیکون اصلی
        Surface(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .shadow(30.dp, CircleShape, spotColor = Color(0xFF2196F3).copy(alpha = 0.5f)),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2196F3),
                                Color(0xFF1976D2),
                                Color(0xFF0D47A1)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Alarm",
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(rotationAngle),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun TaskTitleSection(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔔 یادآوری تسک",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )

            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(onDismiss: () -> Unit, onSnooze: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // دکمه اسنوز
        Button(
            onClick = onSnooze,
            modifier = Modifier
                .weight(1f)
                .height(60.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x33FFFFFF),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Snooze,
                contentDescription = "Snooze",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "اسنوز",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // دکمه خاموش
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .weight(1f)
                .height(60.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEF4444),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "خاموش",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SnoozeOptionsDialog(
    onSnoozeSelected: (minutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val snoozeOptions = listOf(
        1 to "۱ دقیقه",
        5 to "۵ دقیقه",
        10 to "۱۰ دقیقه",
        15 to "۱۵ دقیقه",
        30 to "۳۰ دقیقه",
        60 to "۱ ساعت"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E2E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "⏰ انتخاب زمان اسنوز",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "آلارم بعد از زمان انتخاب شده دوباره زنگ می‌زند",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                snoozeOptions.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowOptions.forEach { (minutes, label) ->
                            SnoozeOptionButton(
                                label = label,
                                modifier = Modifier.weight(1f),
                                onClick = { onSnoozeSelected(minutes) }
                            )
                        }
                        if (rowOptions.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("انصراف", color = Color(0xFF757575), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun SnoozeOptionButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2A2A3E),
            contentColor = Color.White
        )
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getPersianMonthName(month: Int): String {
    return when (month) {
        1 -> "ژانویه"
        2 -> "فوریه"
        3 -> "مارس"
        4 -> "آوریل"
        5 -> "مه"
        6 -> "ژوئن"
        7 -> "ژوئیه"
        8 -> "اوت"
        9 -> "سپتامبر"
        10 -> "اکتبر"
        11 -> "نوامبر"
        12 -> "دسامبر"
        else -> ""
    }
}