package com.kim.minemind.core.history

data class ChangeSet(
    val revealed: Set<Int> = emptySet(),
    val flagged: Set<Int> = emptySet(),
    val gameOver: Boolean = false,
    val win: Boolean = false,
) {
    val empty: Boolean get() =
        revealed.isEmpty() && flagged.isEmpty() && !gameOver && !win

    fun merged(other: ChangeSet): ChangeSet =
        ChangeSet(
            revealed = revealed + other.revealed,
            flagged = flagged + other.flagged,
            gameOver = gameOver || other.gameOver,
            win = win || other.win
        )
}
