package com.kim.minemind.core.board

import com.kim.minemind.shared.BoardSnapshot
import com.kim.minemind.shared.CellSnapshot

fun Board.toSnapshot(): BoardSnapshot =
    BoardSnapshot(
        rows = rows,
        cols = cols,
        mines = mines,
        seed = seed,
        minesPlaced = minesPlaced,
        gameOver = gameOver,
        win = win,
        remainingSafe = remainingSafe,
        cells = cells.map { cell ->
            CellSnapshot(
                gid = cell.gid,
                isMine = cell.isMine,
                isRevealed = cell.isRevealed,
                isFlagged = cell.isFlagged,
                isExploded = cell.isExploded,
                adjacentMines = cell.adjacentMines
            )
        }
    )

fun Board.restoreFromSnapshot(s: BoardSnapshot) {
    require(rows == s.rows && cols == s.cols) { "Board dimensions mismatch" }
    require(mines == s.mines) { "Board mine count mismatch" }
    require(seed == s.seed) { "Board seed mismatch" }

    // These are private-set in your Board; easiest is to add internal setters
    // OR make a restore function inside Board. Since you showed private set,
    // the cleanest is to implement restore *inside* Board as a method.
}
