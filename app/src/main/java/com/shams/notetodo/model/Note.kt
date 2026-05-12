package com.shams.notetodo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.lang.System.*

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = currentTimeMillis()
)