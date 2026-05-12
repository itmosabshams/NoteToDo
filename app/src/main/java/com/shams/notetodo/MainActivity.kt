package com.shams.notetodo

import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.shams.notetodo.db.NoteRepository
import com.shams.notetodo.db.TaskDatabase
import com.shams.notetodo.db.TaskRepository
import com.shams.notetodo.ui.navigation.AppNavGraph
import com.shams.notetodo.ui.theme.NoteToDoTheme
import com.shams.notetodo.viewmodel.NoteViewModel
import com.shams.notetodo.viewmodel.TaskViewModel
import com.shams.notetodo.viewmodel.TaskViewModelFactory
import com.shams.notetodo.viewmodel.NoteViewModelFactory

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


// درخواست اجازه نوتیفیکیشن (Android 13+)
val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    val permission = Manifest.permission.POST_NOTIFICATIONS
    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
        requestPermissionLauncher.launch(permission)
    }
}

        // ساخت دیتابیس‌ها
        val taskDatabase = TaskDatabase.getDatabase(this)
        val noteDatabase = TaskDatabase.getDatabase(this)

        // ساخت ریپازیتوری‌ها
        val taskRepository = TaskRepository(taskDatabase.taskDao())
        val noteRepository = NoteRepository(noteDatabase.noteDao())

        // ساخت ViewModelها با فکتوری
        val taskViewModelFactory = TaskViewModelFactory(taskRepository)
        val noteViewModelFactory = NoteViewModelFactory(noteRepository)

        val taskViewModel = ViewModelProvider(this, taskViewModelFactory)[TaskViewModel::class.java]
        val noteViewModel = ViewModelProvider(this, noteViewModelFactory)[NoteViewModel::class.java]


        setContent {
            NoteToDoTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppNavGraph(
                        navController = navController,
                        taskViewModel = taskViewModel,
                        noteViewModel = noteViewModel
                    )
                }
            }
            // استفاده از ناویگیشن گراف کامل

        }
    }
}