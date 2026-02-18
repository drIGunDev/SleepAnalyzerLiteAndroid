package de.igor.gun.sleep.analyzer.ui.tools.indicators.ppgviewer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ParticleFrameSystem {

    private val _frames = mutableListOf<ParticleFrame>()
    val frames: List<ParticleFrame> get() = _frames

    var isEnrichmentInProgress: Boolean = false
        private set

    private val particlizer = Particlizer()
    private var chunkCollector: ChunkCollector? = null
    private var activeFrame: ParticleFrame? = null
    private var scope: CoroutineScope? = null

    fun setInterpolationInterval(interval: Float) {
        particlizer.interpolationInterval = interval
    }

    fun startEnrichment(chunkCollector: ChunkCollector, scope: CoroutineScope) {
        this.chunkCollector = chunkCollector
        this.scope = scope
        particlizer.start(chunkCollector, scope)
        isEnrichmentInProgress = true
    }

    fun stopEnrichment() {
        particlizer.stop()
        isEnrichmentInProgress = false
        this.scope = null
    }

    fun update(date: Double) {
        if (!isEnrichmentInProgress) return

        enrichFrame()

        if (_frames.isNotEmpty()) {
            _frames.removeAll { it.isDead(after = date) }
        }
    }

    private fun enrichFrame() {
        val particle = particlizer.nextParticle()
        if (particle == null) {
            val collector = chunkCollector
            if (collector != null) {
                scope?.launch { collector.consumingDone() }
            }
            particlizer.frameParticalizingDone()
            activeFrame = null
            return
        }

        val frame = activeFrame
        if (frame != null) {
            frame.addParticle(particle)
        } else {
            val newFrame = ParticleFrame(listOf(particle))
            activeFrame = newFrame
            _frames.add(newFrame)
        }
    }
}
