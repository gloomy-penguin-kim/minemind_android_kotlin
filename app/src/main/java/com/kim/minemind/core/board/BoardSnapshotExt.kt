package com.kim.minemind.core.board

import com.kim.minemind.shared.snapshot.BoardSnapshot
import com.kim.minemind.shared.snapshot.CellSnapshot
import kotlin.div
import kotlin.rem

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
        explodedGid = explodedGid,

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

    val board = Board(rows, cols, mines, seed)
    board.setMinesPlaced(s.minesPlaced)
    board.setGameOver(s.gameOver)
    board.setWin(s.win)
    board.setRemainingSafe(s.remainingSafe)
    board.setExplodedGid(s.explodedGid)
    board.setCells(s.cells)
}

