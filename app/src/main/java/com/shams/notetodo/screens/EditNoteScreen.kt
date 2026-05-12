package com.shams.notetodo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shams.notetodo.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    navController: NavController,
    viewModel: NoteViewModel,
    noteId: Int
) {
    val note = viewModel.getNoteById(noteId)
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }

    if (note == null) {
        // یادداشت پیدا نشد
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("یادداشت مورد نظر پیدا نشد.")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ویرایش یادداشت") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        val updatedNote = note.copy(
                            title = title,
                            content = content
                        )
                        viewModel.updateNote(updatedNote)
                    }
                    navController.popBackStack()
                }
            ) {
                Text("ذخیره")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("محتوا") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10
            )
        }
    }
}
