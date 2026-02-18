package de.igor.gun.sleep.analyzer.ui.tools.indicators.ppgviewer

import de.igor.gun.sleep.analyzer.services.sensors.SensorDataSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChunkCollector(
    private val collectionPeriodSec: Double = PPGViewerV2Config.COLLECTION_PERIOD_SEC,
) {
    private val _frameFlow = MutableSharedFlow<List<Double>>(extraBufferCapacity = 1)
    val frameFlow: SharedFlow<List<Double>> = _frameFlow

    private val mutex = Mutex()
    private val buffer = mutableListOf<SensorDataSource.PPGSamples>()
    private var isConsuming = false

    suspend fun add(chunk: List<SensorDataSource.PPGSamples>) = mutex.withLock {
        if (isConsuming) return@withLock

        buffer.addAll(chunk)
        checkAndTransfer()
    }

    suspend fun consumingDone() = mutex.withLock {
        if (!isConsuming) return@withLock
        buffer.clear()
        isConsuming = false
    }

    private fun checkAndTransfer() {
        if (buffer.size <= 2) return

        val first = buffer.first().timeStamp
        val last = buffer.last().timeStamp
        val intervalNs = last - first
        if (intervalNs < collectionPeriodSec.toLong() * 1_000_000_000L) return

        isConsuming = true
        transferFrame()
    }

    private fun transferFrame() {
        val normalizedBuffer = buffer
            .map { it.channelSamples[0].toDouble() - it.channelSamples[3].toDouble() }
            .subtractMin()
            .lineAdjust()
            .normalize()

        _frameFlow.tryEmit(normalizedBuffer)
    }
}

private fun List<Double>.subtractMin(): List<Double> {
    val min = this.minOrNull() ?: 0.0
    return map { it - min }
}

private fun List<Double>.lineAdjust(): List<Double> {
    val a = firstOrNull() ?: 0.0
    val b = lastOrNull() ?: 0.0
    val n = size.toDouble()
    return mapIndexed { index, element ->
        val l = index / n
        val d = (b - a) * l + a
        element - d
    }
}

private fun List<Double>.normalize(): List<Double> {
    val min = this.minOrNull() ?: 0.0
    val max = this.maxOrNull() ?: 0.0
    val range = max - min
    if (range == 0.0) return map { 0.0 }
    return map { (it - min) / range }
}
