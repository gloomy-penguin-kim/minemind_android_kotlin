package com.kim.minemind.core.history

import kotlinx.serialization.Serializable

@Serializable
data class ChangeSetSnapshot(
    val revealed: Set<Int> = emptySet(),
    val flagged: Set<Int>  = emptySet(),
    val exploded: Set<Int> = emptySet(),
    val explodedGid: Int,
    val gameOver: Boolean = false,
    val win: Boolean = false,
)

fun ChangeSetSnapshot.toChangeSet(): ChangeSet = ChangeSet(
    revealed = revealed,
    flagged  = flagged,
    exploded = exploded,
    explodedGid = explodedGid,
    gameOver = gameOver,
    win = win
)

