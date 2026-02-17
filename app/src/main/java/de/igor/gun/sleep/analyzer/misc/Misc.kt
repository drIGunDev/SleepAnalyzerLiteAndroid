package de.igor.gun.sleep.analyzer.misc

import android.annotation.SuppressLint
import android.graphics.PointF
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


fun LocalDateTime.durationUntilNow(): String {
    return duration(untilDate = LocalDateTime.now())
}

fun LocalDateTime.duration(untilDate: LocalDateTime): String {
    val intervalInSec = ChronoUnit.SECONDS.between(this, untilDate)
    val hour = intervalInSec / (60 * 60)
    val min = (intervalInSec - hour * (60 * 60)) / 60
    val sec = intervalInSec - hour * (60 * 60) - min * 60
    return if (hour > 0) {
        "${hour}h.${min}m.${sec}s."
    } else if (min > 0) {
        "${min}m.${sec}s."
    } else {
        "${sec}s."
    }
}

fun LocalDateTime.durationMillis(untilDate: LocalDateTime): Float = ChronoUnit.MILLIS.between(this, untilDate).toFloat()

fun Float.durationToMillis(defaultInHour: Float): Float = try {
    (this * 60 * 60 * 1000)
} catch (_: Exception) {
    (defaultInHour * 60 * 60 * 1000)
}

@SuppressLint("DefaultLocale")
fun millisToDuration(millis: Float): String {
    val seconds = millis.toLong() / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format("%dh.%dm.", hours, minutes % 60)
    } else if (minutes > 0) {
        String.format("%dm.%ds.", minutes % 60, seconds % 60)
    } else if (seconds > 0) {
        String.format("%ds.", seconds % 60)
    } else {
        ""
    }
}

@SuppressLint("DefaultLocale")
fun millisToDurationCompact(millis: Float): String {
    val seconds = millis.toLong() / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) {
        if (minutes > 0) {
            String.format("%dh.%dm.", hours, minutes % 60)
        } else {
            String.format("%dh.", hours)
        }
    } else if (minutes > 0) {
        String.format("%dm.", minutes % 60)
    } else {
        ""
    }
}

@SuppressLint("DefaultLocale")
fun millisToDurationFull(millis: Float): String {
    val seconds = millis.toLong() / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format("%dh.%dm.%ds.", hours, minutes % 60, seconds % 60)
    } else if (minutes > 0) {
        String.format("%dm.%ds.", minutes % 60, seconds % 60)
    } else if (seconds > 0) {
        String.format("%ds.", seconds % 60)
    } else {
        ""
    }
}

fun String.toInt(default: Int): Int = try {
    this.toInt()
} catch (_: NumberFormatException) {
    default
}

fun String?.toFloat(default: Float): Float = try {
    this?.toFloat() ?: default
} catch (_: NumberFormatException) {
    default
}

fun LocalDateTime.formatToString(
    pattern: String = "yyyy.MM.dd HH:mm",
    patternEnd: String = "HH:mm",
    end: LocalDateTime? = null,
    locale: Locale = Locale.GERMANY
): String {
    val start = DateTimeFormatter
        .ofPattern(pattern, locale)
        .format(this)
    return if (end == null) {
        start
    } else {
        val now = end.formatToString(pattern = patternEnd)
        return "$start - $now"
    }
}

fun LocalDateTime.toMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long =
    this.atZone(zoneId).toInstant().toEpochMilli()

fun LocalDateTime.toSeconds(offset: ZoneOffset = ZoneOffset.UTC): Long =
    this.toEpochSecond(offset)

//time format example "2024-02-06T00:00:00"
fun String.parseLocalDateTimeToMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long {
    val ldt = LocalDateTime.parse(this)
    return ldt.atZone(zoneId).toInstant().toEpochMilli()
}

//time format example "2024-02-06T00:00:00"
fun String.parseLocalDateTimeToSeconds(zoneId: ZoneId = ZoneId.systemDefault()): Long {
    val ldt = LocalDateTime.parse(this)
    return ldt.atZone(zoneId).toEpochSecond()
}

fun LocalDateTime.daysAgoMillis(daysAgo: Int, zoneOffset: ZoneOffset = ZoneOffset.UTC): Long {
    val daysInMillis = daysAgo.toLong() * 24 * 60 * 60 * 1000
    return this.toMillis(zoneOffset) - daysInMillis
}

fun LocalDateTime.daysAfterMillis(daysAfter: Int, zoneOffset: ZoneOffset = ZoneOffset.UTC): Long {
    val daysInMillis = daysAfter.toLong() * 24 * 60 * 60 * 1000
    return this.toMillis(zoneOffset) + daysInMillis
}

fun LocalDateTime.daysRelativeMillis(daysAgoOrAfter: Int, zoneOffset: ZoneOffset = ZoneOffset.UTC): Long =
    if (daysAgoOrAfter < 0) this.daysAgoMillis(-daysAgoOrAfter, zoneOffset) else this.daysAfterMillis(daysAgoOrAfter, zoneOffset)

fun Long.toLocalDateTime(zoneOffset: ZoneOffset = ZoneOffset.UTC): LocalDateTime =
    Instant
        .ofEpochMilli(this)
        .atZone(zoneOffset)
        .toLocalDateTime()

fun List<PointF>.maxX(): Float = this.maxOf { it.x }
fun List<PointF>.minX(): Float = this.minOf { it.x }
fun List<PointF>.maxY(): Float = this.maxOf { it.y }
fun List<PointF>.minY(): Float = this.minOf { it.y }