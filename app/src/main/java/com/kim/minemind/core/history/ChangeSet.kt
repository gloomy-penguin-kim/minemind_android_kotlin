package com.kim.minemind.core.history


data class ChangeSet(
    val revealed: Set<Int> = emptySet(),
    val flagged: Set<Int> = emptySet(),
    val exploded: Set<Int> = emptySet(),
    val explodedGid: Int = -1,
    val gameOver: Boolean = false,
    val win: Boolean = false,
) {

    val empty: Boolean
        get() =
            revealed.isEmpty() && flagged.isEmpty() && exploded.isEmpty() && !gameOver && !win

    fun merged(other: ChangeSet): ChangeSet =
        ChangeSet(
            revealed = revealed + other.revealed,
            flagged = flagged + other.flagged,
            exploded = exploded + other.exploded,
            explodedGid = if (explodedGid == -1) other.explodedGid else explodedGid,
            gameOver = gameOver || other.gameOver,
            win = win || other.win
        )
    fun getAllGid(): Set<Int> = revealed + flagged + exploded

    fun toSnapshot(): ChangeSetSnapshot =
        ChangeSetSnapshot(
            revealed = revealed,
            flagged  = flagged,
            exploded = exploded,
            explodedGid = explodedGid,
            gameOver = gameOver,
            win = win
        )
}

