package com.kim.minemind.core.history

import com.kim.minemind.shared.HistorySnapshot
import com.kim.minemind.shared.snapshot.HistorySnapshot

class HistoryStack {
    private val stack = ArrayDeque<HistoryEntry>()

    fun push(entry: HistoryEntry) { stack.addLast(entry) }
    fun pop(): HistoryEntry? = if (stack.isEmpty()) null else stack.removeLast()
    fun clear() { stack.clear() }
    val size: Int get() = stack.size

    fun toList(): List<HistoryEntry> = stack.toList()
    fun restoreFromList(entries: List<HistoryEntry>) {
        stack.clear()
        entries.forEach { stack.addLast(it) }
    }
}


