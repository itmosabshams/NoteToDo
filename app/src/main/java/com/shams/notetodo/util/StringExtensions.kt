package com.shams.notetodo.util

fun String.toPersianDigits(): String {
    if (isEmpty()) return this

    val persianDigits = mapOf(
        '0' to '۰', '1' to '۱', '2' to '۲', '3' to '۳', '4' to '۴',
        '5' to '۵', '6' to '۶', '7' to '۷', '8' to '۸', '9' to '۹'
    )

    return this.map { char ->
        persianDigits[char] ?: char
    }.joinToString("")
}

fun String.formatDateTime(): Pair<String, String> {
    return try {
        if (isBlank() || this == "-") {
            return Pair("-", "-")
        }

        val parts = trim().split(" ")

        if (parts.size >= 2) {
            val date = parts[0]
            var time = parts[1]

            // استانداردسازی فرمت ساعت
            time = time.replace(":", "")
            when (time.length) {
                3 -> time = "0${time[0]}:${time[1]}${time[2]}"
                4 -> time = "${time[0]}${time[1]}:${time[2]}${time[3]}"
                2 -> time = "00:$time"
                1 -> time = "00:0$time"
            }

            if (!time.contains(":")) {
                time = "${time.take(2)}:${time.drop(2)}"
            }

            val timeParts = time.split(":")
            if (timeParts.size == 2) {
                val hour = timeParts[0].padStart(2, '0').take(2)
                val minute = timeParts[1].padStart(2, '0').take(2)
                Pair(date.toPersianDigits(), "$hour:$minute".toPersianDigits())
            } else {
                Pair(date.toPersianDigits(), time.toPersianDigits())
            }
        } else if (parts.size == 1) {
            Pair(parts[0].toPersianDigits(), "-")
        } else {
            Pair(this.toPersianDigits(), "-")
        }
    } catch (e: Exception) {
        Pair(this.toPersianDigits(), "-")
    }
}