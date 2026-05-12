package com.shams.notetodo.ui.screens


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shams.notetodo.screens.BottomNavigationBar
import com.shams.notetodo.screens.DoneTasksScreen
import com.shams.notetodo.screens.NotesScreen
import com.shams.notetodo.screens.TaskListScreen
import com.shams.notetodo.util.CalendarHeader
import com.shams.notetodo.viewmodel.NoteViewModel
import com.shams.notetodo.viewmodel.TaskViewModel

object BottomNavRoutes {
    const val TASKS = "tasks"
    const val DONE = "done"
    const val NOTES = "notes"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    mainNavController: NavHostController,
    viewModel: TaskViewModel,
    noteViewModel: NoteViewModel
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = bottomNavController)
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            CalendarHeader()

            NavHost(
                navController = bottomNavController,
                startDestination = BottomNavRoutes.TASKS,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(BottomNavRoutes.TASKS) {
                    TaskListScreen(navController = mainNavController, viewModel = viewModel)
                }
                composable(BottomNavRoutes.DONE) {
                    DoneTasksScreen(navController = mainNavController, viewModel = viewModel)
                }
                composable(BottomNavRoutes.NOTES) {
                    NotesScreen(navController = mainNavController, viewModel = noteViewModel)
                }
            }
        }
    }
}
