package de.igor.gun.sleep.analyzer.repositories.tools

data class SleepStateDistribution(val absolutMillis: Map<HypnogramHolder.SleepState, Float>) {
    fun relative(): Map<HypnogramHolder.SleepState, Float> {
        val total = absolutMillis.values.sum()
        return absolutMillis.mapValues { if (total == 0f) 0f else it.value / total }
    }
    val isValid: Boolean get() = absolutMillis.values.sum() > 0
    fun ifValid(block:SleepStateDistribution.()->Unit) {
        if (isValid) block()
    }
}

fun List<SleepSegment>.computeDistribution(): SleepStateDistribution {
    val results = mutableMapOf<HypnogramHolder.SleepState, Float>()
    results.putAll(HypnogramHolder.SleepState.entries.map { Pair(it, 0f) })
    this.forEach {
        results[it.state] = (results[it.state] ?: 0f) + it.durationSeconds * 1000
    }
    return SleepStateDistribution(results)
}
