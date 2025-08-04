package de.igor.gun.sleep.analyzer.misc

import de.igor.gun.sleep.analyzer.db.entities.Measurement
import de.igor.gun.sleep.analyzer.db.entities.Series
import de.igor.gun.sleep.analyzer.services.sensors.SensorDataSource
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


data class MeasurementPoint(
    val hr: Int?,
    val acc: Double?,
    val gyro: Double?,
    val batteryLevel: Int?,
    val rssiLevel: Int?
) {
    fun toMeasurement(date: LocalDateTime, series: Series) =
        Measurement(hr ?: 0, acc ?: 0.0, gyro ?: 0.0, batteryLevel ?: 0, rssiLevel ?: 0, date, series.id)

    val hasNotNullableFields: Boolean = hr != null && acc != null && gyro != null && batteryLevel != null && rssiLevel != null
}

class MeasurementsChecker(private val quantizationIntervalInSec: Int) {
    private var lastMeasurement: List<Any> = listOf()
    private var lastTime: LocalDateTime? = null

    fun getPointIfFits(measurement: List<Any>): MeasurementPoint? {
        synchronized(lastMeasurement) {
            synchronized(measurement) {
                val time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                if (lastTime == null) {
                    lastTime = time
                }
                val interval = lastTime!!.until(time, ChronoUnit.SECONDS)

                return if (interval > quantizationIntervalInSec) {
                    lastMeasurement = measurement
                    lastTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

                    val point = MeasurementPoint(
                        measurement[0] as? Int,
                        (measurement[1] as? SensorDataSource.XYZ)?.rms,
                        (measurement[2] as? SensorDataSource.XYZ)?.rms,
                        measurement[3] as? Int,
                        measurement[4] as? Int
                    )
                    if (point.hasNotNullableFields) {
                        point
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }
    }
}