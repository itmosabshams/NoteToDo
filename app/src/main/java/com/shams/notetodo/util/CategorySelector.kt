package com.shams.notetodo.util

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shams.notetodo.model.TaskCategory

@Composable
fun CategorySelector(
    selected: TaskCategory,
    onCategorySelected: (TaskCategory) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        TaskCategory.entries.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onCategorySelected(category) },
                label = { Text(text = category.name) }
            )
        }
    }
}
