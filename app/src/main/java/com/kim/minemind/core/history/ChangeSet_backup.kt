package com.kim.minemind.core.history

class ChangeSet_backup (
    val revealed: Set<Pair<Int, Int>> = emptySet(),
    val flagged: Set<Pair<Int, Int>> = emptySet(),
    val probabilities: Map<Pair<Int, Int>, Float?> = emptyMap(),
    val gameOver: Boolean = false,
    val win: Boolean = false,
) {
    val empty: Boolean get() =
        revealed.isEmpty() && flagged.isEmpty() && probabilities.isEmpty() && !gameOver && !win

    fun merged(other: ChangeSet): ChangeSet =
        ChangeSet(
            revealed = revealed + other.revealed,
            flagged = flagged + other.flagged,
            probabilities = probabilities + other.probabilities,
            gameOver = gameOver || other.gameOver,
            win = win || other.win
        )
}
