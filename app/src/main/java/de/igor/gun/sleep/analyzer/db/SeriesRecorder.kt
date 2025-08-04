package de.igor.gun.sleep.analyzer.db

import androidx.compose.runtime.mutableStateOf
import de.igor.gun.sleep.analyzer.db.entities.Series
import de.igor.gun.sleep.analyzer.misc.MeasurementPoint
import timber.log.Timber
import java.time.LocalDateTime


class SeriesRecorder(private val dbManager: DBManager) {
    var series: Series? = null
        private set(value) {
            synchronized(this) {
                field = value
                isRecording.value = value != null
            }
        }

    val isRecording = mutableStateOf(false)

    val recordingFlow get() = dbManager.getMeasurementCountAsFlow(series?.id)

    fun startRecording(startDate: LocalDateTime) {
        synchronized(this) {
            if (series == null) {
                series = Series(startDate).apply {
                    this.id = dbManager.insertSeries(series = this)
                }
            } else {
                Timber.w("you have first to stop recording!")
            }
        }
    }

    fun stopRecording(
        endDate: LocalDateTime,
        satisfaction: Series.Satisfaction = Series.Satisfaction.NEUTRAL,
    ) {
        synchronized(this) {
            series?.let { series ->
                series.satisfaction = satisfaction.value
                series.endDate = endDate
                dbManager.updateSeries(series)
            }
            series = null
        }
    }

    fun recordMeasurementPoint(point: MeasurementPoint) {
        synchronized(this) {
            series?.let {
                val measurement = point.toMeasurement(LocalDateTime.now(), it)
                dbManager.insertMeasurement(measurement)
            }
        }
    }
}