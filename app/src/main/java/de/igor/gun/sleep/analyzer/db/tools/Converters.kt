package de.igor.gun.sleep.analyzer.db.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.room.TypeConverter
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime


class Converters {
    @TypeConverter
    fun fromSleepState(value: HypnogramHolder.SleepState) = value.value

    @TypeConverter
    fun sleepStateFromInt(value: Int) = HypnogramHolder.SleepState.findOrDefault(value)

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromBase64ToBitmap(base64Value: String): Bitmap {
        val decodedBytes = Base64.decode(base64Value, 0)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    @TypeConverter
    fun fromBitmapToBase64(bitmap: Bitmap): String? {
        val byteArrayOS = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS)
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT)
    }
}