package de.igor.gun.sleep.analyzer.ui.tools.indicators.ppgviewer

data class Particle(
    val creationDate: Double,
    val y: Double,
) {
    fun isDead(after: Double): Boolean =
        creationDate < after - PPGViewerV2Config.COLLECTION_PERIOD_SEC * 10
}
