package de.igor.gun.sleep.analyzer.db.entities

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder


@Entity(
    tableName = "Cache",
    foreignKeys = [
        ForeignKey(
            entity = Series::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("series_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["series_id"])
    ]
)
class Cache(
    @ColumnInfo(name = "max_hr") var maxHR: Float,
    @ColumnInfo(name = "min_hr") var minHR: Float,
    @ColumnInfo(name = "max_hr_scaled") var maxHRScaled: Float,
    @ColumnInfo(name = "min_hr_scaled") var minHRScaled: Float,
    @ColumnInfo(name = "duration") var duration: Float,
    @ColumnInfo(name = "chart_image") var chartImage: Bitmap,
    @ColumnInfo(name = "awake") var awake: Float,
    @ColumnInfo(name = "rem") var rem: Float,
    @ColumnInfo(name = "l_sleep") var lSleep: Float,
    @ColumnInfo(name = "d_sleep") var dSleep: Float,
    @ColumnInfo(name = "series_id") var seriesId: Long,
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
)

fun Cache.toSleepStateDistribution(): HypnogramHolder.SleepStateDistribution =
    HypnogramHolder.SleepStateDistribution(
        mapOf(
            HypnogramHolder.SleepState.AWAKE to awake,
            HypnogramHolder.SleepState.LIGHT_SLEEP to lSleep,
            HypnogramHolder.SleepState.DEEP_SLEEP to dSleep,
            HypnogramHolder.SleepState.REM to rem
        )
    )