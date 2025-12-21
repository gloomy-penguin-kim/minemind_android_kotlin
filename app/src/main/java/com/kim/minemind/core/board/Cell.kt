package com.kim.minemind.core.board

data class Cell(
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