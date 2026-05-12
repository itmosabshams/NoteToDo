package com.shams.notetodo.db

import com.shams.notetodo.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    val getAllTasks: Flow<List<Task>> = dao.getAllTasks()

    suspend fun insertTask(task: Task): Long = dao.insertTask(task)
    suspend fun updateTask(task: Task) = dao.updateTask(task)
    suspend fun deleteTask(task: Task) = dao.deleteTask(task)
    suspend fun getTaskById(id: Int): Task? = dao.getTaskById(id)
}
