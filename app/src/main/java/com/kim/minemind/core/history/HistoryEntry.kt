package com.kim.minemind.core.history

import com.kim.minemind.shared.Move

data class HistoryEntry(
    val move: Move,
    val changes: ChangeSet,
    val moveCountBefore: Int,
    val note: String = ""
)
