package com.kim.minemind.core.history

data class ChangeSet(
    val revealed: Set<Int> = emptySet(),
    val flagged: Set<Int> = emptySet(),
    val exploded: Set<Int> = emptySet(),
    val gameOver: Boolean = false,
    val win: Boolean = false,
) {

    val empty: Boolean get() =
        revealed.isEmpty() && flagged.isEmpty() && exploded.isEmpty() && !gameOver && !win

    fun merged(other: ChangeSet): ChangeSet =
        ChangeSet(
            revealed = revealed + other.revealed,
            flagged = flagged + other.flagged,
            exploded = exploded + other.exploded,

            gameOver = gameOver || other.gameOver,
            win = win || other.win
        )

    fun getAllGid(): Set<Int> = revealed + flagged + exploded
}
