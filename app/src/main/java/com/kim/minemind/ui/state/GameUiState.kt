package com.kim.minemind.ui.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.kim.minemind.analysis.AnalyzerOverlay
import com.kim.minemind.analysis.rules.Rule
import com.kim.minemind.core.TapMode
import com.kim.minemind.shared.ConflictList


//data class GameVisualState(
//    val shuffledGlyphs: List<String>? = null,
//    val shuffledColors: List<Long>? = null,
//)

data class GameUiState(
    val rows: Int = 9,
    val cols: Int = 9,
    val mines: Int = 10,
    val moves: Int = 0,
    val gameOver: Boolean = false,
    val win: Boolean = false,
    val tapMode: TapMode = TapMode.OPEN,
//    val cells: List<CellUI> = emptyList(),
    val cells: SnapshotStateList<CellUI> = mutableStateListOf(),

    val conflictBoard: ConflictList = ConflictList(),
    val conflictProbs: ConflictList = ConflictList(),
    val ruleList: Map<Int, Rule> = emptyMap(),
    val overlay: AnalyzerOverlay = AnalyzerOverlay(),
    val isVerify: Boolean = false,
    val isEnumerate: Boolean = false,
)


//fun GameUiState.resetOverlay(): GameUiState {
//    return this.copy(
//        overlay = AnalyzerOverlay(),
//        ruleList = emptyMap(),
//        conflictBoard = ConflictList(),
//
//    return this
//}



