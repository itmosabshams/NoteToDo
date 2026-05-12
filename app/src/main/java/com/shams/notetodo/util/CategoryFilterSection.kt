package com.shams.notetodo.util

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shams.notetodo.model.TaskCategory

@Composable
fun CategoryFilterSection(
    selectedCategory: TaskCategory,
    onCategorySelected: (TaskCategory) -> Unit
) {
    val categories = listOf(
        TaskCategory.ALL,
        TaskCategory.PERSONAL,
        TaskCategory.BUY,
        TaskCategory.DAILY,
        TaskCategory.COSTS,
        TaskCategory.INSTALLMENTS,
        TaskCategory.MEETING,
        TaskCategory.SPORT
    )

    val accentColor = Color(0xFF03A9F4) // آبی روشن
    val unselectedColor = Color(0xFFF5F5F5)
    val textSelectedColor = Color.White
    val textUnselectedColor = Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory

            Surface(
                modifier = Modifier
                    .defaultMinSize(minWidth = 70.dp)
                    .height(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onCategorySelected(category) },
                color = if (isSelected) accentColor else unselectedColor,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = if (isSelected) 6.dp else 2.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .background(Color.Transparent)
                ) {
                    Text(
                        text = category.toPersianName(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) textSelectedColor else textUnselectedColor
                    )
                }
            }
        }
    }
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
