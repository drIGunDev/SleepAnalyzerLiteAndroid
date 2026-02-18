package de.igor.gun.sleep.analyzer.ui.tools.indicators.ppgviewer

class ParticleFrame(particles: List<Particle> = emptyList()) {

    private val _particles = particles.toMutableList()
    val particles: List<Particle> get() = _particles

    fun addParticle(particle: Particle) {
        _particles.add(particle)
    }

    fun isDead(after: Double): Boolean =
        _particles.isEmpty() || _particles.all { it.isDead(after) }
}
