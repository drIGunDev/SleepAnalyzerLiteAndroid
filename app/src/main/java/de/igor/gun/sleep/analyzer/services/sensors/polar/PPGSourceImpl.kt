package de.igor.gun.sleep.analyzer.services.sensors.polar

import de.igor.gun.sleep.analyzer.services.sensors.PPGSource
import de.igor.gun.sleep.analyzer.services.sensors.SensorDataSource
import de.igor.gun.sleep.analyzer.services.sensors.SensorDataSource.PPGSamples
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject


class PPGSourceImpl @Inject constructor(
    private val dataSource: SensorDataSource,
) : PPGSource {

    private var xResolution: Int = 1

    override fun setXResolution(xResolution: Int) = run { this.xResolution = xResolution; this }

    private var height: Float = DEFAULT_HEIGHT

    override fun setHeight(height: Float) = run { this.height = height; this }

    private var maxBufferSize: Int = MAX_BUFFER_SIZE

    override fun setMaxBufferSize(maxBufferSize: Int) = run { this.maxBufferSize = maxBufferSize; this }

    override val ppgFlow = MutableStateFlow<PPGSource.PPGChunk?>(null)

    private var streamingJob: Job? = null

    override fun startStreaming() {
        fun truncateBuffer() {
            if (buffer.size >= maxBufferSize / xResolution) {
                buffer.subList(0, buffer.size - (maxBufferSize / xResolution)).clear()
            }
        }

        fun producePPGFlow() {
            synchronized(samples) {
                if (samples.isNotEmpty()) {
                    val n = (instrument.measurementInterval / ticker.delay / 1_000_000).toInt()
                    val samplesToShow = if (n > 0) samples.size / n else samples.size
                    buffer.addAll(samples.subList(0, samplesToShow))
                    samples.subList(0, samplesToShow).clear()
                }
                truncateBuffer()
                if (buffer.isNotEmpty()) ppgFlow.value = PPGSource.PPGChunk(buffer, height, xResolution)
            }
        }

        stopStreaming()
        clear()
        streamingJob = CoroutineScope(Dispatchers.Default).launch {
            launch {
                dataSource
                    .ppgFlow
                    .collect {
                        consumeSamplesFrom(source = it)
                    }
            }

            launch {
                ticker
                    .start()
                    .flowOn(Dispatchers.Default)
                    .buffer()
                    .collect {
                        producePPGFlow()
                    }
            }
        }
    }

    override fun stopStreaming() {
        ticker.cancel()
        streamingJob?.cancel()
        streamingJob?.cancelChildren()
        streamingJob = null
    }

    private companion object {
        const val SAMPLE_BUFFER_SIZE = 500
        const val MAX_BUFFER_SIZE = 400
        const val DEFAULT_HEIGHT = 100f
    }

    private class Ticker(val delay: Long = DELAY) {

        fun start() = flow {
            var i = 0
            isAlive = true
            while (isAlive) {
                emit(i++)
                delay(delay)
            }
        }

        fun cancel() = run { isAlive = false }

        private var isAlive = true

        companion object {
            const val DELAY = 100L
        }
    }

    private class Instrument {
        private var lastTimeStamp: Long = 0
        var measurementInterval: Long = 0

        fun computeMeasurementInterval(chunk: List<PPGSamples>) {
            if (lastTimeStamp == 0L) {
                lastTimeStamp = chunk.first().timeStamp
            }
            measurementInterval = chunk.last().timeStamp - lastTimeStamp
            lastTimeStamp = chunk.last().timeStamp
        }
    }

    private val instrument = Instrument()
    private val ticker = Ticker()
    private val samples: MutableList<Float> = mutableListOf()
    private val buffer: MutableList<Float> = mutableListOf()

    private fun consumeSamplesFrom(source: List<PPGSamples>, channelId: Int = 0) {
        fun normalize(array: List<Float>): List<Float> {
            val max = array.max()
            val min = array.min()
            return array.map { (max - it) / (max - min) }
        }

        fun removeAmbilight() = source
            .map { Pair(it.channelSamples[channelId], it.channelSamples[3]) }
            .map { it.first.toFloat() - it.second.toFloat() }

        fun truncateSamples() {
            if (samples.size >= SAMPLE_BUFFER_SIZE) {
                samples.subList(0, SAMPLE_BUFFER_SIZE).clear()
            }
        }

        if (source.isEmpty()) return

        synchronized(samples) {
            instrument.computeMeasurementInterval(source)
            val chunk = removeAmbilight()
            val normalizedChunk = normalize(chunk)
            samples.addAll(normalizedChunk)
            truncateSamples()
        }
    }

    private fun clear() = run { samples.clear(); buffer.clear() }
}