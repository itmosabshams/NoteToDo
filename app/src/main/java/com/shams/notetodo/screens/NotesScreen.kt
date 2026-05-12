package com.shams.notetodo.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.NavHostController
import com.shams.notetodo.components.NoteItem
import com.shams.notetodo.ui.navigation.Routes
import com.shams.notetodo.vieewmodel.NoteViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    navController: NavHostController,
    viewModel: NoteViewModel
) {
    val notes by viewModel.notes.collectAsState() // StateFlow<List<Note>>

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    navController.navigate(Routes.ADD_NOTE)
                }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "افزودن یادداشت")
                }
            },
            floatingActionButtonPosition = FabPosition.Start // ✅ همیشه راست پایین
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Text(
                    text = "یادداشت‌ها",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes.size) { index ->
                        val note = notes[index]
                        NoteItem(
                            note = note,
                            onClick = {
                                navController.navigate(Routes.readOnlyNoteRoute(note.id))
                            },
                            onEditClick = {
                                navController.navigate(Routes.editNoteRoute(note.id))
                            },
                            onDeleteClick = {
                                viewModel.deleteNote(note)
                            }
                        )
                    }
                }
            }
        }
    }
}
