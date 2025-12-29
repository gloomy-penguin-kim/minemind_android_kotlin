package com.kim.minemind.ui.state

import com.kim.minemind.analysis.analyzer.AnalyzerOverlay
import com.kim.minemind.core.Action
import com.kim.minemind.core.TapMode
import com.kim.minemind.core.board.Cell


data class GameUiState(
    val rows: Int = 9,
    val cols: Int = 9,
    val mines: Int = 10,
    val moves: Int = 0,
    val gameOver: Boolean = false,
    val win: Boolean = false,
    val tapMode: TapMode = TapMode.OPEN,
    val cells: List<CellUI> = emptyList(),
    val overlay: AnalyzerOverlay? = null,

    val isVerify: Boolean = false,
    val isEnumerate: Boolean = false,
)

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
    val conflict: Boolean = false,        // from overlay
//    val ruleAction: Action?       // from overlay
)



