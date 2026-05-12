package com.shams.notetodo.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shams.notetodo.model.Task
import com.shams.notetodo.model.TaskCategory
import com.shams.notetodo.util.CategoryFilterSection
import com.shams.notetodo.ui.components.TaskItem
import com.shams.notetodo.ui.navigation.Routes.editTaskRoute
import com.shams.notetodo.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TaskListScreen(
    navController: NavHostController,
    viewModel: TaskViewModel
) {
    
    val context = LocalContext.current
val allTasks by viewModel.tasks.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val filteredTasks = when (selectedCategory) {
        TaskCategory.ALL -> allTasks.filter { !it.isDone }
        else -> allTasks.filter { it.category == selectedCategory && !it.isDone }
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isBottomSheetVisible by remember { mutableStateOf(false) }

    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val fabColor = Color(0xFF03A9F4) // آبی روشن

    Scaffold(
        contentColor = Color.Transparent, // حذف پس‌زمینه خاکستری
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp),
                contentAlignment = Alignment.BottomStart // چون RTL هست، Start = راست
            ) {
                FloatingActionButton(
                    onClick = { isBottomSheetVisible = true },
                    containerColor = fabColor
                ) {
                    Icon(Icons.Default.Add, contentDescription = "افزودن تسک", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Rtl),
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Rtl)
                    // پایین حذف شده: paddingValues.calculateBottomPadding()
                )
                .background(Color.White) // پس‌زمینه سفید (یا هر رنگ دلخواه)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                CategoryFilterSection(
                    selectedCategory = selectedCategory,
                    onCategorySelected = viewModel::setCategory
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("تسکی برای نمایش وجود ندارد.")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            val dismissState = rememberDismissState(
                                confirmStateChange = { dismissValue ->
                                    when (dismissValue) {
                                        DismissValue.DismissedToEnd -> {
                                            navController.navigate(editTaskRoute(task.id))
                                            false
                                        }
                                        DismissValue.DismissedToStart -> {
                                            taskToDelete = task
                                            showDeleteDialog = true
                                            false
                                        }
                                        else -> false
                                    }
                                }
                            )

                            SwipeToDismiss(
                                state = dismissState,
                                directions = setOf(
                                    DismissDirection.StartToEnd,
                                    DismissDirection.EndToStart
                                ),
                                background = {
                                    val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                                    val color = when (direction) {
                                        DismissDirection.StartToEnd -> Color(0xFF4CAF50)
                                        DismissDirection.EndToStart -> Color.Red
                                    }
                                    val icon = when (direction) {
                                        DismissDirection.StartToEnd -> Icons.Default.Edit
                                        DismissDirection.EndToStart -> Icons.Default.Delete
                                    }
                                    val alignment = when (direction) {
                                        DismissDirection.StartToEnd -> Alignment.CenterStart
                                        DismissDirection.EndToStart -> Alignment.CenterEnd
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = alignment
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                },
                                dismissContent = {
                                    TaskItem(
                                        task = task,
                                        onToggle = { updatedTask ->
                                            viewModel.toggleTaskDone(context, updatedTask)
                                        }
                                    )
                                },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showDeleteDialog && taskToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    taskToDelete = null
                },
                title = { Text("حذف تسک") },
                text = { Text("آیا مطمئن هستید که می‌خواهید این تسک را حذف کنید؟") },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            taskToDelete?.let { viewModel.deleteTask(it) }
                            showDeleteDialog = false
                            taskToDelete = null
                        }
                    }) {
                        Text("حذف")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        taskToDelete = null
                    }) {
                        Text("انصراف")
                    }
                }
            )
        }

        if (isBottomSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { isBottomSheetVisible = false },
                sheetState = bottomSheetState
            ) {
                AddTaskBottomSheet(
                    viewModel = viewModel,
                    onDismiss = { isBottomSheetVisible = false }
                )
            }
        }
    }
}