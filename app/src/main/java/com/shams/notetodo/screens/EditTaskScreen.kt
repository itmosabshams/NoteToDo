package com.shams.notetodo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shams.notetodo.model.Task
import com.shams.notetodo.model.TaskCategory
import com.shams.notetodo.vieewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    taskId: Int
) {
    // تسک مورد نظر را پیدا می‌کنیم
    val allTasks by viewModel.tasks.collectAsState()
    val taskToEdit = allTasks.find { it.id == taskId }

    // اگر تسک پیدا نشد، صفحه خالی یا پیام خطا نشان می‌دهیم
    if (taskToEdit == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack() // بازگشت خودکار
        }
        return
    }

    // وضعیت‌های محلی برای فرم ویرایش
    var title by remember { mutableStateOf(taskToEdit.title) }
    var description by remember { mutableStateOf(taskToEdit.description) }
    var category by remember { mutableStateOf(taskToEdit.category) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ویرایش تسک") },
                actions = {
                    TextButton(onClick = {
                        val updatedTask = taskToEdit.copy(
                            title = title,
                            description = description,
                            category = category
                        )
//                        viewModel.updateTask(updatedTask)
                        navController.popBackStack()
                    }) {
                        Text("ذخیره", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("توضیحات") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("دسته‌بندی:", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            DropdownMenuBox(
                selectedCategory = category,
                onCategorySelected = { category = it }
            )
        }
    }
}

@Composable
fun DropdownMenuBox(
    selectedCategory: TaskCategory,
    onCategorySelected: (TaskCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedCategory.name)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskCategory.values().filter { it != TaskCategory.ALL }.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}
