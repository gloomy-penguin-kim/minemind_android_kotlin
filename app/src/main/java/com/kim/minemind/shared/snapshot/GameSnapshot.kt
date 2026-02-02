package com.kim.minemind.shared.snapshot

import com.kim.minemind.core.Action
import com.kim.minemind.core.history.ChangeSet
import com.kim.minemind.core.history.ChangeSetSnapshot
import com.kim.minemind.core.history.HistoryEntrySnapshot
import com.kim.minemind.core.history.HistoryEvent
import com.kim.minemind.core.history.HistoryEventSnapshot
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class GameSnapshot(
    val board: BoardSnapshot,
    val moves: Int,
)

@Serializable
data class MoveRecord(
    val gid: Int,
    val action: Action,
    val timestamp: Long
)

@Serializable
data class BoardSnapshot(
    val rows: Int,
    val cols: Int,
    val mines: Int,
    val seed: Int,
    val firstClickGid: Int,
    val minesPlaced: Boolean,
    val gameOver: Boolean,
    val win: Boolean,
    val remainingSafe: Int,
    val explodedGid: Int,
    val cells: List<CellSnapshot>
)

@Serializable
data class CellSnapshot(
    val gid: Int,
    val isMine: Boolean,
    val isRevealed: Boolean,
    val isFlagged: Boolean,
    val isExploded: Boolean,
    val isExplodedGid: Boolean,
    val adjacentMines: Int
)

@Serializable
data class HistorySnapshot(
    val entries: List<HistoryEntrySnapshot>
)


@Serializable
data class HistoryEventSnapshot(
    val type: Type,
    val action: String? = null,     // Action.name
    val gid: Int? = null,
    val flagValue: Boolean? = null,
    val count: Int? = null,
    val note: String? = null
) {
    @Serializable
    enum class Type {
        USER_COMMAND,
        AUTO_STEP,
        APPLY_RECOMMENDATIONS,
        SYSTEM
    }
}

fun userCommandEvent(action: Action, gid: Int, flagValue: Boolean? = null) =
    HistoryEventSnapshot(
        type = HistoryEventSnapshot.Type.USER_COMMAND,
        action = action.name,
        gid = gid,
        flagValue = flagValue
    )

fun autoStepEvent(note: String = "") =
    HistoryEventSnapshot(
        type = HistoryEventSnapshot.Type.AUTO_STEP,
        note = note
    )


private fun ChangeSetSnapshot.toChangeSet(): ChangeSet {
    return ChangeSet(
        revealed = revealed,
        flagged = flagged,
        exploded = exploded,
        explodedGid = explodedGid,
        gameOver = gameOver,
        win = win
    )
}

private fun HistoryEventSnapshot.toEvent(): HistoryEvent {
    return when (type) {
        HistoryEventSnapshot.Type.USER_COMMAND -> {
            HistoryEvent.UserCommand(
                action = Action.valueOf(action!!),
                gid = gid,
                flagValue = flagValue
            )
        }
        HistoryEventSnapshot.Type.AUTO_STEP -> {
            HistoryEvent.Auto(note = note)
        }
        HistoryEventSnapshot.Type.APPLY_RECOMMENDATIONS -> {
            HistoryEvent.ApplyRecommendations(count = count)
        }
        HistoryEventSnapshot.Type.SYSTEM -> {
            HistoryEvent.System(note = note)
        }
    }
}