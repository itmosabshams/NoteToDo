package com.shams.notetodo.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shams.notetodo.screens.*
import com.shams.notetodo.ui.screens.MainScreen
import com.shams.notetodo.viewmodel.NoteViewModel
import com.shams.notetodo.viewmodel.TaskViewModel

object Routes {
    const val MAIN = "main"
    const val ADD_TASK = "add_task"
    const val EDIT_TASK = "edit_task/{taskId}"
    fun editTaskRoute(taskId: Int) = "edit_task/$taskId"
    const val DONE_TASKS = "doneTasks"
    const val NOTES = "notes"
    const val ADD_NOTE = "add_note"
    const val EDIT_NOTE = "edit_note/{noteId}"
    fun editNoteRoute(noteId: Int) = "edit_note/$noteId"
    const val READ_ONLY_NOTE = "read_only_note/{noteId}"
    fun readOnlyNoteRoute(noteId: Int) = "read_only_note/$noteId"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    taskViewModel: TaskViewModel,
    noteViewModel: NoteViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN
    ) {
        composable(Routes.MAIN) {
            MainScreen(
                mainNavController = navController,
                viewModel = taskViewModel,
                noteViewModel = noteViewModel
            )
        }

        composable(
            route = Routes.EDIT_TASK,
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            taskId?.let {
                EditTaskScreen(
                    navController = navController,
                    viewModel = taskViewModel,
                    taskId = it
                )
            }
        }

        composable(Routes.DONE_TASKS) {
            DoneTasksScreen(
                navController = navController,
                viewModel = taskViewModel
            )
        }

        composable(Routes.NOTES) {
            NotesScreen(
                navController = navController,
                viewModel = noteViewModel
            )
        }

        composable(Routes.ADD_NOTE) {
            AddNoteScreen(
                navController = navController,
                viewModel = noteViewModel
            )
        }

        // صفحه جزئیات یادداشت با انیمیشن نرم مثل آیفون
        composable(
            route = Routes.READ_ONLY_NOTE,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType }),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(400, easing = LinearEasing)
                ) + scaleIn(
                    initialScale = 1f,
                    animationSpec = tween(400, easing = LinearEasing)
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(400, easing = LinearEasing)
                ) + scaleOut(
                    targetScale = 1f,
                    animationSpec = tween(400, easing = LinearEasing)
                )
            }
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
            val note = noteViewModel.getNoteById(noteId)
            if (note != null) {
                ReadOnlyNoteScreen(
                    navController = navController,
                    note = note
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(
            route = Routes.EDIT_NOTE,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId")
            noteId?.let {
                EditNoteScreen(
                    navController = navController,
                    viewModel = noteViewModel,
                    noteId = it
                )
            }
        }
    }
}