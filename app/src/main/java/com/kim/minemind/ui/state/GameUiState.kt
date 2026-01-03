package com.kim.minemind.ui.state

import com.kim.minemind.analysis.AnalyzerOverlay
import com.kim.minemind.ui.settings.DisplaySettings
import com.kim.minemind.core.TapMode
import com.kim.minemind.shared.ConflictList
import com.kim.minemind.shared.Move


data class GameVisualState(
    val shuffledGlyphs: List<String>? = null,
    val shuffledColors: List<Long>? = null,
)

data class GameUiState(
    val rows: Int = 9,
    val cols: Int = 9,
    val mines: Int = 10,
    val moves: Int = 0,
    val gameOver: Boolean = false,
    val win: Boolean = false,
    val tapMode: TapMode = TapMode.OPEN,
    val cells: List<CellUI> = emptyList(),
    val conflictBoard: ConflictList = ConflictList(),
    val conflictProbs: ConflictList = ConflictList(),
    val ruleList: Map<Int, Move> = emptyMap(),
    val overlay: AnalyzerOverlay = AnalyzerOverlay(),
    val isVerify: Boolean = false,
    val isEnumerate: Boolean = false,

    val settings: DisplaySettings = DisplaySettings(),
    val visual: GameVisualState = GameVisualState()
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

    val conflict: Boolean = false
//    val ruleAction: Action?       // from overlay
)




