package com.shams.notetodo.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.shams.notetodo.model.TaskCategory

@Composable
fun DropdownMenuCategory(
    selected: TaskCategory,
    onSelected: (TaskCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("دسته‌بندی: ${selected.name.lowercase().replaceFirstChar { it.uppercase() }}")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TaskCategory.values().filter { it != TaskCategory.ALL }.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(category.name.lowercase().replaceFirstChar { it.uppercase() })
                    },
                    onClick = {
                        onSelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
