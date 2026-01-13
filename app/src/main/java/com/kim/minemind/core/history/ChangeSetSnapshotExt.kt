package com.kim.minemind.core.history

fun ChangeSet.toSnapshot(): ChangeSetSnapshot =
    ChangeSetSnapshot(
        revealed = revealed,
        flagged  = flagged,
        exploded = exploded,
        explodedGid = explodedGid,
        gameOver = gameOver,
        win = win
    )
