package de.igor.gun.sleep.analyzer.misc

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import java.util.LinkedList

//
//LRU — least recently used cache policy.
//
class ReactiveLRUCache<Key, Value>(
    private val maxEntries: Int
) {
    private val map = mutableStateMapOf<Key, Value>()
    private val queue = LinkedList<Key>()

    fun get(id: Key): Value? = map[id]

    fun put(id: Key, wrapper: Value) {
        if (map.containsKey(id)) return
        map[id] = wrapper
        queue.add(id)

        if (queue.size > maxEntries) {
            val removeId = queue.removeFirst()
            map.remove(removeId)
        }
    }

    fun remove(id: Key) {
        map.remove(id)
        queue.remove(id)
    }

    fun clear() {
        map.clear()
        queue.clear()
    }

    fun asStateMap(): SnapshotStateMap<Key, Value> = map
}
