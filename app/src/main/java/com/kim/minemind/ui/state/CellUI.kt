package com.kim.minemind.ui.state

data class CellUI(
    val gid: Int,
    val isMine: Boolean,
    val isRevealed: Boolean,
    val isFlagged: Boolean,
    val isExploded: Boolean,
    val adjacentMines: Int,

    val probability: Float?,      // from overlay

    var forcedOpen: Boolean = false,     // from overlay
    var forcedFlag: Boolean = false,

    val conflict: Boolean = false
//    val ruleAction: Action?       // from overlay
)
