package com.kim.minemind.core.history

import kotlinx.serialization.Serializable

@Serializable
data class HistoryEntrySnapshot(
    val event: HistoryEventSnapshot,
    val changes: ChangeSetSnapshot,
    val moveCountBefore: Int,
    val remainingSafeBefore: Int
)
