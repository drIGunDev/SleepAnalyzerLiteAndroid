package de.igor.gun.sleep.analyzer.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.igor.gun.sleep.analyzer.db.entities.Series.Satisfaction
import de.igor.gun.sleep.analyzer.db.tools.Converters
import de.igor.gun.sleep.analyzer.misc.duration
import de.igor.gun.sleep.analyzer.misc.durationMillis
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@Entity(tableName = "Series")
data class Series(
    @ColumnInfo(name = "start_date") val startDate: LocalDateTime,
    @ColumnInfo(name = "end_date") var endDate: LocalDateTime? = null,
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var satisfaction: Int = Satisfaction.NEUTRAL.value,
) : Parcelable {

    enum class Satisfaction(val value: Int) {
        BAD(0) {
            override fun toEmoji(): String = "☹️"
        },
        NEUTRAL(1) {
            override fun toEmoji(): String = "😐"
        },
        GOOD(2) {
            override fun toEmoji(): String = "😇"
        };

        abstract fun toEmoji(): String
    }

    constructor(parcel: Parcel) : this(
        Converters().fromTimestamp(parcel.readString())!!,
        Converters().fromTimestamp(parcel.readString()),
        parcel.readLong(),
        parcel.readInt()
    )

    override fun toString(): String {
        return if (endDate != null) {
            "${dateFormatter(startDate)} (${startDate.duration(untilDate = endDate!!)})"
        } else {
            dateFormatter(startDate)
        }
    }

    val durationMillis: Float
        get() =
            endDate
                ?.let { startDate.durationMillis(untilDate = endDate!!) }
                ?: startDate.durationMillis(untilDate = LocalDateTime.now())

    companion object {
        fun dateFormatter(date: LocalDateTime): String =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.GERMANY).format(date)

        @JvmField
        var CREATOR = object : Parcelable.Creator<Series> {
            override fun createFromParcel(parcel: Parcel): Series {
                return Series(parcel)
            }

            override fun newArray(size: Int): Array<Series?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        with(parcel) {
            writeString(Converters().dateToTimestamp(startDate))
            writeString(Converters().dateToTimestamp(endDate))
            writeLong(id)
            writeInt(satisfaction)
        }
    }

    override fun describeContents(): Int {
        return 0
    }
}

fun Int.toSatisfaction(): Satisfaction =
    try {
        Satisfaction.entries.first { it.value == this }
    } catch (_: NoSuchElementException) {
        Satisfaction.NEUTRAL
    }

fun Series.satisfactionToEmoji() = satisfaction.toSatisfaction().toEmoji()