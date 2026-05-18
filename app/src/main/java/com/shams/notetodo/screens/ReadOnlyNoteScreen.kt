package com.shams.notetodo.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shams.notetodo.model.Note
import com.shams.notetodo.ui.navigation.Routes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadOnlyNoteScreen(
    navController: NavController,
    note: Note
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var startAnimation by remember { mutableStateOf(false) }

    // انیمیشن نرم و کشسانی هنگام ورود
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // مقیاس با افکت کشسانی (باز شدن نرم)
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,  // کشسانی ملایم
            stiffness = Spring.StiffnessVeryLow          // خیلی نرم
        ),
        label = "scale"
    )

    // محو شدن همزمان
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = note.title,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        startAnimation = false
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Routes.editNoteRoute(note.id))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش"
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .alpha(contentAlpha)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "📅 ${note.createdAt}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = note.content,
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف یادداشت") },
            text = { Text("آیا از حذف این یادداشت مطمئن هستید؟") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    navController.previousBackStackEntry?.savedStateHandle?.set("delete_note", note.id)
                    startAnimation = false
                    navController.popBackStack()
                }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}