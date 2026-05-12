package com.shams.notetodo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shams.notetodo.model.Note
import com.shams.notetodo.viewmodel.NoteViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    navController: NavController,
    viewModel: NoteViewModel
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("افزودن یادداشت") }
            )
        },




        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        val newNote = Note(
                            title = title,
                            content = content,
                            timestamp = Date().time
                        )

                        
                        viewModel.addNote(newNote)
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
