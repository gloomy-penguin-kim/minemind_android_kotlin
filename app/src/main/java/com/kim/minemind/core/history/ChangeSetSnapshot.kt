package com.kim.minemind.core.history

data class ChangeSetSnapshot(
    val revealed: IntArray,
     val flagged: IntArray,
    val exploded: IntArray,
    val explodedGid: Int,
    val gameOver: Boolean = false,
    val win: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChangeSetSnapshot

        if (explodedGid != other.explodedGid) return false
        if (gameOver != other.gameOver) return false
        if (win != other.win) return false
        if (!revealed.contentEquals(other.revealed)) return false
        if (!flagged.contentEquals(other.flagged)) return false
        if (!exploded.contentEquals(other.exploded)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = explodedGid
        result = 31 * result + gameOver.hashCode()
        result = 31 * result + win.hashCode()
        result = 31 * result + revealed.contentHashCode()
        result = 31 * result + flagged.contentHashCode()
        result = 31 * result + exploded.contentHashCode()
        return result
    }
}

fun ChangeSetSnapshot.toChangeSet(): ChangeSet = ChangeSet(
    revealed = revealed.toSet(),
    flagged = flagged.toSet(),
    exploded = exploded.toSet(),
    explodedGid = explodedGid,
    gameOver = gameOver,
    win = win
)

