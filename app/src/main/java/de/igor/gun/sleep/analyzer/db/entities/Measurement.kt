package de.igor.gun.sleep.analyzer.db.entities

import android.graphics.PointF
import androidx.compose.ui.graphics.toArgb
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.igor.gun.sleep.analyzer.repositories.tools.ChartBuilder
import de.igor.gun.sleep.analyzer.ui.theme.Blue
import de.igor.gun.sleep.analyzer.ui.theme.DarkRed
import de.igor.gun.sleep.analyzer.ui.theme.Green
import de.igor.gun.sleep.analyzer.ui.theme.Red
import de.igor.gun.sleep.analyzer.ui.theme.Yellow
import java.time.Duration
import java.time.LocalDateTime


@Entity(
    tableName = "Measurement",
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
data class Measurement(
    val hr: Int,
    val acc: Double,
    val gyro: Double,
    @ColumnInfo(name = "battery_level") val batteryLevel: Int,
    @ColumnInfo(name = "rssi_level") val rssiLevel: Int,
    val date: LocalDateTime,
    @ColumnInfo(name = "series_id") var seriesId: Long,
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
) {
    enum class Id {
        HR, ACC, GYRO, BATTERY, RSSI
    }
}

fun List<Measurement.Id>.toIdWithColor(): List<Pair<Measurement.Id, Int>> =
    this.map {
        when (it) {
            Measurement.Id.HR -> Pair(Measurement.Id.HR, Red.toArgb())
            Measurement.Id.ACC -> Pair(Measurement.Id.ACC, Green.toArgb())
            Measurement.Id.GYRO -> Pair(Measurement.Id.GYRO, Blue.toArgb())
            Measurement.Id.RSSI -> Pair(Measurement.Id.RSSI, DarkRed.toArgb())
            Measurement.Id.BATTERY -> Pair(Measurement.Id.BATTERY, Yellow.toArgb())
        }
    }

fun List<Measurement>.toChart(
    idWithColor: List<Pair<Measurement.Id, Int>>,
): Map<Measurement.Id, ChartBuilder.ChartPresentation.Chart> = run {
    if (this.isEmpty() || idWithColor.isEmpty()) {
        mapOf()
    } else {
        val channels = mutableMapOf<Measurement.Id, Pair<ArrayList<PointF>, Int>>().apply {
            idWithColor.forEach {
                this[it.first] = Pair(arrayListOf(), it.second)
            }
        }
        for (measurement in this) {
            val duration = Duration.between(this.first().date, measurement.date).toMillis().toFloat()
            for (item in idWithColor) {
                when (item.first) {
                    Measurement.Id.HR -> channels[Measurement.Id.HR]?.first?.add(PointF(duration, measurement.hr.toFloat()))
                    Measurement.Id.ACC -> channels[Measurement.Id.ACC]?.first?.add(PointF(duration, measurement.acc.toFloat()))
                    Measurement.Id.GYRO -> channels[Measurement.Id.GYRO]?.first?.add(PointF(duration, measurement.gyro.toFloat()))
                    Measurement.Id.BATTERY -> channels[Measurement.Id.BATTERY]?.first?.add(PointF(duration, measurement.batteryLevel.toFloat()))
                    Measurement.Id.RSSI -> channels[Measurement.Id.RSSI]?.first?.add(PointF(duration, measurement.rssiLevel.toFloat()))
                }
            }
        }
        mutableMapOf<Measurement.Id, ChartBuilder.ChartPresentation.Chart>().apply {
            idWithColor.forEach {
                val id = it.first
                val color = it.second
                this[id] = ChartBuilder.ChartPresentation.Chart(
                    id.name,
                    channels[id]?.first!!,
                    color = color,
                    yAxisLettering = it.first.name == Measurement.Id.HR.name
                )
            }
        }
    }
}