package com.shams.notetodo.vieewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shams.notetodo.db.TaskRepository
import com.shams.notetodo.model.Task
import com.shams.notetodo.model.TaskCategory
import com.shams.notetodo.utils.AlarmHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(TaskCategory.ALL)
    val selectedCategory: StateFlow<TaskCategory> = _selectedCategory.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    val doneTasks: StateFlow<List<Task>> = _tasks
        .map { it.filter { task -> task.isDone } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notDoneTasks: StateFlow<List<Task>> = _tasks
        .map { it.filter { task -> !task.isDone } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            repository.getAllTasks.collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    fun addTask(context: Context, task: Task, alarmCalendar: Calendar) {
    viewModelScope.launch {
        // Room insert returns the generated id
        val newId = repository.insertTask(task).toInt()
        val savedTask = task.copy(id = newId)

        // schedule alarm only if task is not done and time is in future
        if (!savedTask.isDone && alarmCalendar.timeInMillis > System.currentTimeMillis()) {
            AlarmHelper.scheduleTaskAlarm(context, savedTask, alarmCalendar)
        }
    }
}


    fun updateTask(context: Context, task: Task, alarmCalendar: Calendar? = null) {
    viewModelScope.launch {
        repository.updateTask(task)

        // If task is done => cancel existing alarm
        if (task.isDone) {
            AlarmHelper.cancelTaskAlarm(context, task.id)
            return@launch
        }

        // If a new date/time is provided, reschedule
        if (alarmCalendar != null) {
            AlarmHelper.cancelTaskAlarm(context, task.id)
            if (alarmCalendar.timeInMillis > System.currentTimeMillis()) {
                AlarmHelper.scheduleTaskAlarm(context, task, alarmCalendar)
            }
        }
    }
}


    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleTaskDone(context: Context, task: Task) {
    viewModelScope.launch {
        val updatedTask = task.copy(isDone = !task.isDone)
        repository.updateTask(updatedTask)

        if (updatedTask.isDone) {
            AlarmHelper.cancelTaskAlarm(context, updatedTask.id)
        }
    }
}


    fun setCategory(category: TaskCategory) {
        _selectedCategory.value = category
    }

    fun getTaskById(taskId: Int): Task? {
        return _tasks.value.find { it.id == taskId }
    }
}