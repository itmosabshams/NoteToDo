package com.shams.notetodo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shams.notetodo.model.TaskCategory

@Composable
fun CategoryTabs(
    selected: TaskCategory,
    onCategorySelected: (TaskCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskCategory.values().forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onCategorySelected(category) },
                label = { Text(text = category.name.lowercase().replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}