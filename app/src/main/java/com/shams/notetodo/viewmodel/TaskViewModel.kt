package com.shams.notetodo.viewmodel

import android.content.Context
import android.util.Log
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
        Log.d("AlarmDebug323", "===================")
        Log.d("AlarmDebug323", "Saving to DB - dateTimeString: ${task.dateTime}")
        Log.d("AlarmDebug323", "Saving to DB - alarmCalendar time: ${alarmCalendar.time}")
        Log.d("AlarmDebug323", "===================")
        viewModelScope.launch {
            // اول Task را در دیتابیس ذخیره کن
            val newId = repository.insertTask(task).toInt()
            val savedTask = task.copy(id = newId)

            // حالا _tasks آپدیت می‌شود
            _tasks.value = _tasks.value + savedTask

            // فقط اگر Task انجام نشده و زمان آلارم در آینده است، آلارم را تنظیم کن
            if (!savedTask.isDone && alarmCalendar.timeInMillis > System.currentTimeMillis()) {
                // اصلاح: اینجا باید taskId و title را جداگانه بفرستی
                AlarmHelper.scheduleTaskAlarm(
                    context,
                    savedTask.id,      // taskId
                    savedTask.title,   // title
                    alarmCalendar
                )
            }
        }
    }

    fun updateTask(context: Context, task: Task, alarmCalendar: Calendar? = null) {
        viewModelScope.launch {
            repository.updateTask(task)

            // اگر Task انجام شده، آلارم را کنسل کن
            if (task.isDone) {
                AlarmHelper.cancelTaskAlarm(context, task.id)
                return@launch
            }

            // اگر تاریخ/ساعت جدید داده شده، دوباره آلارم تنظیم کن
            if (alarmCalendar != null) {
                AlarmHelper.cancelTaskAlarm(context, task.id)
                if (alarmCalendar.timeInMillis > System.currentTimeMillis()) {
                    AlarmHelper.scheduleTaskAlarm(
                        context,
                        task.id,
                        task.title,
                        alarmCalendar
                    )
                }
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _tasks.value = _tasks.value.filter { it.id != task.id }
        }
    }

    fun toggleTaskDone(context: Context, task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isDone = !task.isDone)
            repository.updateTask(updatedTask)

            // آپدیت لیست
            _tasks.value = _tasks.value.map {
                if (it.id == task.id) updatedTask else it
            }

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