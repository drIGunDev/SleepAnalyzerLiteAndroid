package de.igor.gun.sleep.analyzer.misc

import android.content.Context
import de.igor.gun.sleep.analyzer.BuildConfig
import de.igor.gun.sleep.analyzer.db.entities.Measurement


class AppParameters(
    val context: Context
) {
    var frameSizeHR: Int
        get() = AppSettings(context).frameSizeHR
        set(value) {
            AppSettings(context).frameSizeHR = value
        }
    var frameSizeACC: Int
        get() = AppSettings(context).frameSizeACC
        set(value) {
            AppSettings(context).frameSizeACC = value
        }
    var quantizationHR: Float
        get() = AppSettings(context).quantizationHR
        set(value) {
            AppSettings(context).quantizationHR = value
        }
    var quantizationACC: Float
        get() = AppSettings(context).quantizationACC
        set(value) {
            AppSettings(context).quantizationACC = value
        }
    val isDebugVersion = BuildConfig.DEBUG
    val splashScreenDelay = 1000L
    val measurementIds: List<Measurement.Id>
        get() =
            if (isDebugVersion)
                listOf(Measurement.Id.HR, Measurement.Id.ACC, Measurement.Id.GYRO, Measurement.Id.RSSI)
            else
                listOf(Measurement.Id.HR)

    val minSignificantIntervalSec: Double = 60.0
    val minAwakeDurationSec: Double = 10 * 60.0
    val hrHiPassCutoff: Double = 80.0
    val accHiPassCutoff: Double = 700.0

    enum class AppEntryPoints {
        TRACKING, ARCHIVE, ARCHIVE_DETAIL
    }

    fun getMeasurementIds(entryPoint: AppEntryPoints): List<Measurement.Id> {
        return when (entryPoint) {
            AppEntryPoints.TRACKING -> listOf(Measurement.Id.HR)
            AppEntryPoints.ARCHIVE -> listOf(Measurement.Id.HR)
            AppEntryPoints.ARCHIVE_DETAIL -> measurementIds
        }
    }

    fun saveToPreferences() {
        val settings = AppSettings(context)
        settings.frameSizeHR = this.frameSizeHR
        settings.frameSizeACC = this.frameSizeACC
        settings.quantizationHR = this.quantizationHR
        settings.quantizationACC = this.quantizationACC
    }

    fun initFromPreferences() {
        val settings = AppSettings(context)
        this.frameSizeHR = settings.frameSizeHR
        this.frameSizeACC = settings.frameSizeACC
        this.quantizationHR = settings.quantizationHR
        this.quantizationACC = settings.quantizationACC
    }
}