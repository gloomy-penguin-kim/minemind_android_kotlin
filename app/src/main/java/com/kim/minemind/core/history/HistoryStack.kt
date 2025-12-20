package com.kim.minemind.core.history

class HistoryStack {
    private val stack = ArrayDeque<HistoryEntry>()

    fun push(entry: HistoryEntry) { stack.addLast(entry) }
    fun pop(): HistoryEntry? = if (stack.isEmpty()) null else stack.removeLast()
    fun clear() { stack.clear() }
    val size: Int get() = stack.size
}