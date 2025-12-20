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
    val cells: List<Cell> = emptyList(),
)

data class CellUi(
    val r: Int,
    val c: Int,
    val revealed: Boolean,
    val flagged: Boolean,
    val exploded: Boolean,
    val adj: Int,
    val mine: Boolean = false,
    val probability: Float? = null
)
