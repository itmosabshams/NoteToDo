package com.shams.notetodo.model

enum class TaskCategory {
    ALL, PERSONAL, BUY, DAILY, COSTS, INSTALLMENTS, MEETING, SPORT
}

// نام فارسی دسته‌بندی‌ها
fun TaskCategory.toPersianName(): String = when (this) {
    TaskCategory.ALL -> "همه"
    TaskCategory.PERSONAL -> "شخصی"
    TaskCategory.BUY -> "خرید"
    TaskCategory.DAILY -> "روزانه"
    TaskCategory.COSTS -> "هزینه‌ها"
    TaskCategory.INSTALLMENTS -> "اقساط"
    TaskCategory.MEETING -> "جلسه"
    TaskCategory.SPORT -> "ورزش"
}
