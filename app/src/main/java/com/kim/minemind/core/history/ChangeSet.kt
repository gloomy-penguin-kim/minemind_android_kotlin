package com.kim.minemind.core.history

data class ChangeSet(
    val revealed: Set<Int> = emptySet(),
    val flagged: Set<Int> = emptySet(),
    val exploded: Set<Int> = emptySet(),
    val conflicts: Map<Int, MutableSet<String>> = LinkedHashMap(),

    val gameOver: Boolean = false,
    val win: Boolean = false,
) {
    val empty: Boolean get() =
        revealed.isEmpty() && flagged.isEmpty() && exploded.isEmpty() && conflicts.isEmpty() && !gameOver && !win

    fun merged(other: ChangeSet): ChangeSet =
        ChangeSet(
            revealed = revealed + other.revealed,
            flagged = flagged + other.flagged,
            exploded = exploded + other.exploded,
            conflicts = conflicts + other.conflicts,
            gameOver = gameOver || other.gameOver,
            win = win || other.win
        )

    fun getAllGid() = revealed + flagged + exploded + conflicts.keys
}
