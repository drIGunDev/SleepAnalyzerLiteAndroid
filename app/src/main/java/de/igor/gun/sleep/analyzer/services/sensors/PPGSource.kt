package de.igor.gun.sleep.analyzer.services.sensors

import android.graphics.PointF
import kotlinx.coroutines.flow.StateFlow


interface PPGSource {
    val ppgFlow: StateFlow<PPGChunk?>

    class PPGChunk(
        buffer: List<Float>,
        height: Float,
        xResolution: Int
    ) {
        val data: List<PointF>

        init {
            val max: Float = buffer.max()
            val min: Float = buffer.min()
            fun toScreenY(y: Float) = (1f - ((y - min) / (max - min))) * height
            var x = 0f
            data = buffer.map { y ->
                x += xResolution
                PointF(x, toScreenY(y))
            }
        }
    }

    fun setXResolution(xResolution: Int): PPGSource
    fun setHeight(height: Float): PPGSource
    fun setMaxBufferSize(maxBufferSize: Int): PPGSource

    fun startStreaming()
    fun stopStreaming()
}