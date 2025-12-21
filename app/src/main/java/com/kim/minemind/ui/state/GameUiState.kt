package com.kim.minemind.ui.state

import com.kim.minemind.core.board.Cell

data class GameUiState(
    val rows: Int = 9,
    val cols: Int = 9,
    val mines: Int = 10,
    val moves: Int = 0,
    val gameOver: Boolean = false,
    val win: Boolean = false,
    val flagMode: Boolean = false,
    val cells: List<CellUI> = emptyList(),
)


data class CellUI(
    val row: Int,
    val col: Int,
    val gid: Int,
    var isMine: Boolean = false,
    var isRevealed: Boolean = false,
    var isFlagged: Boolean = false,
    var isExploded: Boolean = false,
    var adjacentMines: Int = 0,
    var probability: Float? = null
)