package com.shams.notetodo.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate

data class ShamsiDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    override fun toString(): String {
        return "$year/${month.toString().padStart(2, '0')}/${day.toString().padStart(2, '0')}"
    }
}