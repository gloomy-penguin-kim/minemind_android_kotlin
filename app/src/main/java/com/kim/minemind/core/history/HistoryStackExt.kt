package com.kim.minemind.core.history

import com.kim.minemind.shared.snapshot.HistorySnapshot

fun HistoryStack.toSnapshot(): HistorySnapshot =
    HistorySnapshot(
        entries = toList().map { it.toSnapshot() }
    )

fun HistoryStack.restore(s: HistorySnapshot) {
    restoreFromList(s.entries.map { it.toEntry() })
}