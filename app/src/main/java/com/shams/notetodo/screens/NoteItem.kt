package com.shams.notetodo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shams.notetodo.model.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // سایز داینامیک با تفاوت بیشتر
    val titleLength = note.title.length
    val contentLength = note.content.length

    val dynamicHeight = when {
        contentLength > 200 -> 280.dp
        contentLength > 120 -> 230.dp
        contentLength > 60 -> 190.dp
        contentLength > 20 -> 160.dp
        titleLength > 40 -> 170.dp
        titleLength > 20 -> 150.dp
        else -> 135.dp
    }

    val cardColor = getCardColor(note.id)
    var showDeleteDialog by remember { mutableStateOf(false) }

    // تاریخ میلادی (بدون ساعت)
    val dateOnly = remember(note.createdAt) {
        try {
            val parts = note.createdAt.split(" ")
            parts[0]
        } catch (e: Exception) {
            note.createdAt
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(dynamicHeight)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = listOf(cardColor, Color.White))
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // عنوان
            Text(
                text = note.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // متن
            val maxLines = when {
                contentLength < 50 -> 2
                contentLength < 120 -> 3
                else -> 4
            }

            Text(
                text = note.content,
                fontSize = 12.sp,
                color = Color(0xFF666666),
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // خط جداکننده
            Divider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = Color(0xFFEEEEEE)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // فوتر: حذف - تاریخ - ویرایش در یک خط
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // دکمه حذف
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // تاریخ (با حداکثر عرض ثابت و عدم رفتن به خط بعد)
                Text(
                    text = dateOnly,
                    fontSize = 11.sp,
                    color = Color(0xFF999999),
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, true),
                    textAlign = TextAlign.Center
                )

                // دکمه ویرایش
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "ویرایش",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // دیالوگ حذف
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "حذف یادداشت",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "آیا از حذف یادداشت «${note.title}» مطمئن هستید؟",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text("حذف", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("انصراف", color = Color(0xFF999999))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

private fun getCardColor(id: Int): Color {
    val colors = listOf(
        Color(0xFFEEF4FF),
        Color(0xFFEDFCF2),
        Color(0xFFFFF4E6),
        Color(0xFFFFF0F0),
        Color(0xFFF3EEFF),
        Color(0xFFE6FFFA),
        Color(0xFFFFFBE6),
        Color(0xFFFFE6F0)
    )
    return colors[id % colors.size]
}