package com.kim.minemind.core.history

import com.kim.minemind.shared.HistoryEntrySnapshot


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

fun HistoryEntrySnapshot.toHistoryEntry(): HistoryEntry =
    HistoryEntry(
        event = event.toEvent(),
        changes = changes.toChangeSet(),
        moveCountBefore = moveCountBefore,
        remainingSafeBefore = remainingSafeBefore
    )