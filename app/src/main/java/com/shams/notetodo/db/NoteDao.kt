package com.shams.notetodo.db

import androidx.room.*
import com.shams.notetodo.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // تغییر timestamp به createdAt
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: Int): Note?
}