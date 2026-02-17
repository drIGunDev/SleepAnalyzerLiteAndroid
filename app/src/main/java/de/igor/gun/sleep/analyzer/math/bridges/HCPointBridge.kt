package de.igor.gun.sleep.analyzer.math.bridges

// Bridge: HCPoint <-> UnPoint (app-level type)
// Uncomment when UnPoint is available from the app module.

import android.graphics.PointF
import android.os.Build
import androidx.annotation.RequiresApi
import de.igor.gun.sleep.analyzer.db.entities.Measurement
import de.igor.gun.sleep.analyzer.hypnogram.computation.v2.classes.HCPoint
import de.igor.gun.sleep.analyzer.misc.toMillis
import java.time.LocalDateTime


fun HCPoint.toPointF(): PointF = PointF(x.toFloat(), y.toFloat())

fun List<HCPoint>.toPointFs(): List<PointF> = map { it.toPointF() }

fun Measurement.toHCPoint(id: Measurement.Id, startTime: LocalDateTime): HCPoint {
    @RequiresApi(Build.VERSION_CODES.S)
    fun toMillis(date: LocalDateTime): Double = date.toMillis().toDouble()
    return when (id) {
        Measurement.Id.HR -> HCPoint(x = toMillis(date), y = hr.toDouble())
        Measurement.Id.ACC -> HCPoint(x = toMillis(date), y = acc)
        Measurement.Id.GYRO -> HCPoint(x = toMillis(date), y = gyro)
        Measurement.Id.BATTERY -> HCPoint(x = toMillis(date), y = batteryLevel.toDouble())
        Measurement.Id.RSSI -> HCPoint(x = toMillis(date), y = rssiLevel.toDouble())
    }
}

fun List<Measurement>.toHCPoint(id: Measurement.Id): List<HCPoint> =
    map {
        it.toHCPoint(id, this[0].date)
    }