package com.kim.minemind.core.history

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


data class HistoryEntry(
    val event: HistoryEvent,
    val changes: ChangeSet,
    val moveCountBefore: Int,
    val remainingSafeBefore: Int
)

fun HistoryEntry.toSnapshot(): HistoryEntrySnapshot =
    HistoryEntrySnapshot(
        event = event.toSnapshot(),
        changes = changes.toSnapshot(),
        moveCountBefore = moveCountBefore,
        remainingSafeBefore = remainingSafeBefore
    )


fun HistoryEntrySnapshot.toEntry(): HistoryEntry =
    HistoryEntry(
        event = event.toEvent(),
        changes = changes.toChangeSet(),
        moveCountBefore = moveCountBefore,
        remainingSafeBefore = remainingSafeBefore
    )

@Serializable
data class HistoryEntrySnapshot(
    val event: HistoryEventSnapshot,
    @Contextual val changes: ChangeSetSnapshot, // this line
    val moveCountBefore: Int,
    val remainingSafeBefore: Int
)
