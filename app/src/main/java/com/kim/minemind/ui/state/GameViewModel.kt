package com.kim.minemind.ui.state

import androidx.lifecycle.ViewModel
import com.kim.minemind.analysis.AnalysisConfig
import com.kim.minemind.analysis.Analyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.kim.minemind.core.*
import com.kim.minemind.analysis.Solver
import com.kim.minemind.analysis.analyzer.AnalyzerOverlay
import com.kim.minemind.analysis.rules.Move
import com.kim.minemind.core.board.Board
import com.kim.minemind.core.history.ChangeSet
import com.kim.minemind.core.history.HistoryEntry
import com.kim.minemind.core.history.HistoryStack

import android.util.Log

class GameViewModel : ViewModel() {
    companion object {
        private const val TAG = "ui.GameViewModel"
    }

    private val config = AnalysisConfig
    private var board: Board = Board(rows = 25, cols = 25, mines = 200, seed = 0)
    private var solver: Solver = Solver()
    private val analyzer: Analyzer = Analyzer()
    private val history = HistoryStack()
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    private fun idx(r: Int, c: Int, cols: Int) = r * cols + c



    init {
        newGame(rows=25, cols= 25, mines=150)
    }

    fun newGame(rows: Int, cols: Int, mines: Int) {
        board = Board(rows, cols, mines, seed = 0)
        solver.clear()
        analyzer.clear()
        history.clear()
        _uiState.value = GameUiState(
            rows = rows,
            cols = cols,
            mines = mines,
            moves = 0,
            cells = buildCells(board)
        )
    }

    fun toggleFlagMode() {
        _uiState.update { it.copy(flagMode = !it.flagMode) }
    }

    fun dispatch(action: Action, gid: Int) {
        val beforeMoves = _uiState.value.moves

        val truthDelta = board.apply(action, gid) // <-- TRUTH ONLY
        if (truthDelta.empty) return

        history.push(HistoryEntry(Move(gid, action, MoveKind.USER),truthDelta, beforeMoves))

        val overlay = analyzer.analyze(board)

        Log.d(TAG, "overlay = $overlay")
        Log.d(TAG, "forcedFlags = ${overlay.forcedFlags}")
        Log.d(TAG, "forcedOpens = ${overlay.forcedOpens}")
        Log.d(TAG, "probs = ${overlay.probabilities}")


        _uiState.update { s ->
            s.copy(
                moves = beforeMoves + 1,
                gameOver = truthDelta.gameOver,
                win = truthDelta.win,
                cells = patchCells(
                    base = s.cells,
                    cs = truthDelta,
                    board = board,
                    overlay = overlay
                )
            )
        }
    }


    private fun patchCells(
        base: List<CellUI>,
        cs: ChangeSet,
        board: Board,
        overlay: AnalyzerOverlay
    ): List<CellUI> {
        val updated = base.toMutableList()

        val changed = HashSet<Int>().apply {
            addAll(cs.revealed)
            addAll(cs.flagged)

            // overlay-driven changes
            addAll(overlay.forcedOpens)
            addAll(overlay.forcedFlags)
            addAll(overlay.conflicts)
            addAll(overlay.probabilities.keys)
        }

        for (gid in changed) {
            val bc = board.cells[gid]

            val p =
                if (overlay.probabilities.containsKey(gid)) overlay.probabilities[gid]
                else updated[gid].probability

            for (gid in changed) {
                val bc = board.cells[gid]

                updated[gid] = CellUI(
                    gid = gid,
                    isMine = bc.isMine,
                    isRevealed = bc.isRevealed,
                    isFlagged = bc.isFlagged,
                    isExploded = bc.isExploded,
                    adjacentMines = bc.adjacentMines,

                    probability = overlay.probabilities[gid],
                    forcedOpen = gid in overlay.forcedOpens,
                    forcedFlag = gid in overlay.forcedFlags,
                    conflict = gid in overlay.conflicts,
                )
            }

        }

        return updated
    }



//    fun undo() {
//        val entry = history.pop() ?: return
//        board.undo(entry)
//
//        val components = solver.buildFrontier(board)
//        val probs = probabilityEngine.computeProbabilities(board, components)
//
//        _uiState.update { s ->
//            val rebuilt = buildCells(board)
//            s.copy(
//                moves = entry.moveCountBefore,
//                gameOver = false,
//                win = false,
//                cells = rebuilt.mapIndexed { gid, cell ->
//                    cell.copy(probability = probs[gid] ?: cell.probability)
//                }
//            )
//        }
//    }

    fun undo() {
        val entry = history.pop() ?: return
        board.undo(entry) // revert truth only

        val overlay = analyzer.analyze(board)

        _uiState.update { s ->
            s.copy(
                moves = entry.moveCountBefore,
                gameOver = false,
                win = false,
                cells = buildCells(board, overlay)
            )
        }
    }

    private fun buildCells(b: Board, overlay: AnalyzerOverlay? = null): List<CellUI> {
        val out = ArrayList<CellUI>(b.rows * b.cols)
        for (gid in 0 until (b.rows * b.cols)) {
            val bc = b.cells[gid]
            out.add(
                CellUI(
                    gid = gid,
                    isMine = bc.isMine,
                    isRevealed = bc.isRevealed,
                    isFlagged = bc.isFlagged,
                    isExploded = bc.isExploded,
                    adjacentMines = bc.adjacentMines,
                    probability = overlay?.probabilities?.get(gid),
                    conflict = overlay?.conflicts?.contains(gid) ?: false,
                )
            )
        }
        return out
    }


}
