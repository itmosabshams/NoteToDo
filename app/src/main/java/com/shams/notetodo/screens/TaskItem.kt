package com.shams.notetodo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.shams.notetodo.model.Task
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

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("confetti.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = showAnimation,
        iterations = 1,
        speed = 1.5f
    )

    val shakeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // تبدیل اعداد انگلیسی به فارسی
    fun String.toPersianDigits(): String {
        val english = "0123456789"
        val persian = "۰۱۲۳۴۵۶۷۸۹"
        var result = this
        english.forEachIndexed { index, c ->
            result = result.replace(c, persian[index])
        }
        return result
    }

    val (datePart, timePart) = task.dateTime.split(" ").let { Pair(it.getOrNull(0) ?: "-", it.getOrNull(1) ?: "-") }
    val persianDate = datePart.toPersianDigits()
    val persianTime = timePart.toPersianDigits()

    LaunchedEffect(showAnimation) {
        if (showAnimation) {
            coroutineScope.launch {
                repeat(4) {
                    shakeOffset.animateTo(-6f, tween(30))
                    shakeOffset.animateTo(6f, tween(30))
                }
                shakeOffset.animateTo(0f, tween(40))
            }
            delay(1000)
            onToggle(task)
        }
    }

    val accentBlue = Color(0xFF03A9F4)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .graphicsLayer { translationX = shakeOffset.value }
            .border(
                width = if (showAnimation) 2.dp else 0.dp,
                color = if (showAnimation) accentBlue else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(
            containerColor = if (showAnimation) Color.White else if (task.isDone) Color(0xFFE0E0E0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
            if (showAnimation) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LottieAnimation(composition, progress = { progress }, modifier = Modifier.size(100.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "آفرین! تسک انجام شد 🎉",
                        style = MaterialTheme.typography.titleMedium.copy(color = accentBlue),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "عنوان : "+ task.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "دسته بندی: ${task.category.toPersianName()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                        Checkbox(
                            checked = showChecked || task.isDone,
                            onCheckedChange = {
                                if (!task.isDone) {
                                    showChecked = true
                                    coroutineScope.launch {
                                        delay(150)
                                        showAnimation = true
                                    }
                                } else {
                                    onToggle(task)
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = accentBlue)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.LightGray.copy(alpha = 0.5f),
                                        Color.LightGray.copy(alpha = 0.75f),
                                        Color.LightGray.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "تاریخ : $persianDate",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1
                        )
                        Text(
                            text = " ساعت : $persianTime",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}