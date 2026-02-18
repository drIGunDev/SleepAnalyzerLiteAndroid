package de.igor.gun.sleep.analyzer.ui.tools.indicators.ppgviewer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class Particlizer {

    @Volatile
    var interpolationInterval: Float = 0f
    private var collectorJob: Job? = null

    private val lock = Any()
    private val interpolatedFrame = mutableListOf<Double>()

    fun start(chunkCollector: ChunkCollector, scope: CoroutineScope) {
        collectorJob = scope.launch {
            chunkCollector.frameFlow.collect { frame ->
                if (frame.isEmpty()) return@collect
                val interval = interpolationInterval
                if (interval <= 0f) return@collect
                interpolateFrame(frame, interval)
            }
        }
    }

    fun stop() {
        collectorJob?.cancel()
        collectorJob = null
        synchronized(lock) { interpolatedFrame.clear() }
    }

    fun nextParticle(): Particle? = synchronized(lock) {
        if (interpolatedFrame.isEmpty()) return@synchronized null

        val y = interpolatedFrame.removeAt(0)
        Particle(
            creationDate = System.nanoTime() / 1_000_000_000.0,
            y = y,
        )
    }

    fun frameParticalizingDone() = synchronized(lock) {
        interpolatedFrame.clear()
    }

    private fun interpolateFrame(frame: List<Double>, interval: Float) = synchronized(lock) {
        interpolatedFrame.clear()

        val n = frame.size
        if (n < 2) return@synchronized

        val w = interval.toInt().coerceAtLeast(1)
        val d = (n - 1).toDouble() / w.toDouble()

        for (i in 0..w) {
            val ix = d * i
            val lo = ix.toInt().coerceAtMost(n - 1)
            val hi = (lo + 1).coerceAtMost(n - 1)
            val frac = ix - lo
            val y = frame[lo] + (frame[hi] - frame[lo]) * frac
            interpolatedFrame.add(y)
        }
    }
}
