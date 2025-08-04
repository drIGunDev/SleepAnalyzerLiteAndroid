package de.igor.gun.sleep.analyzer.misc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.zip


fun <T> combineFlows(vararg flows: Flow<T>): Flow<List<T>> {
    if (flows.size < 2) throw IllegalArgumentException("flows must have at least two")

    val seadFlow = flows[0].combine(flows[1]) { value1, value2 ->
        val mediator = mutableListOf<T>()
        synchronized(mediator) {
            mediator.add(value1)
            mediator.add(value2)
            mediator
        }
    }

    if (flows.size == 2) return seadFlow

    return (2..<flows.size)
        .map { flows[it] }
        .fold(seadFlow) { combinedFlows, flow ->
            synchronized(combinedFlows) {
                combinedFlows.combine(flow) { mediator, value ->
                    mediator.add(value)
                    mediator
                }
            }
        }
}

fun <T> zipFlows(vararg flows: Flow<T>): Flow<List<T>> {
    if (flows.size < 2) throw IllegalArgumentException("flows must have at least two")

    val seadFlow = flows[0].zip(flows[1]) { value1, value2 ->
        val mediator = mutableListOf<T>()
        synchronized(mediator) {
            mediator.add(value1)
            mediator.add(value2)
            mediator
        }
    }

    if (flows.size == 2) return seadFlow

    return (2..<flows.size)
        .map { flows[it] }
        .fold(seadFlow) { combinedFlows, flow ->
            synchronized(combinedFlows) {
                combinedFlows.zip(flow) { mediator, value -> mediator.add(value); mediator }
            }
        }
}
