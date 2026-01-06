package com.kim.minemind.shared
import com.kim.minemind.core.Action
import com.kim.minemind.core.history.ChangeSetSnapshot
import com.kim.minemind.core.history.HistoryEvent
import com.kim.minemind.core.history.HistoryEventSnapshot
import kotlinx.serialization.Serializable


@Serializable
data class GameSnapshot(
    val board: BoardSnapshot,
    val history: HistorySnapshot
)

@Serializable
data class BoardSnapshot(
    val rows: Int,
    val cols: Int,
    val mines: Int,
    val seed: Int,
    val minesPlaced: Boolean,
    val gameOver: Boolean,
    val win: Boolean,
    val remainingSafe: Int,
    val cells: List<CellSnapshot>
)

@Serializable
data class CellSnapshot(
    val gid: Int,
    val isMine: Boolean,
    val isRevealed: Boolean,
    val isFlagged: Boolean,
    val isExploded: Boolean,
    val adjacentMines: Int
)

@Serializable
data class HistorySnapshot(
    val entries: List<HistoryEntrySnapshot>
)

@Serializable
data class HistoryEntrySnapshot(
    val event: HistoryEventSnapshot,
    val changes: ChangeSetSnapshot,
    val moveCountBefore: Int,
    val remainingSafeBefore: Int
)

@Serializable
data class MoveSnapshot(
    // adjust to match your Move type; common fields:
    val gid: Int,
    val action: Action,
    val kind: String, // "OPEN" / "FLAG" / "CHORD" etc
    val flagValue: Boolean? = null
)

@Serializable
data class ChangeSetSnapshot(
    val revealed: List<Int> = emptyList(),
    val flagged: List<Int> = emptyList(),
    val probabilities: Map<Int, Float?> = emptyMap(),
    val gameOver: Boolean = false,
    val win: Boolean = false,
)

private fun HistoryEvent.toEvent(): HistoryEvent {
    HIstoryEvent(
        event = event.toSnapshot(),
    )
}
