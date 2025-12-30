package com.kim.minemind.ui.state

import androidx.lifecycle.ViewModel
import com.kim.minemind.analysis.AnalysisConfig
import com.kim.minemind.analysis.Analyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.kim.minemind.core.*
import com.kim.minemind.analysis.Solver
import com.kim.minemind.analysis.AnalyzerOverlay
import com.kim.minemind.shared.Move
import com.kim.minemind.core.board.Board
import com.kim.minemind.core.history.ChangeSet
import com.kim.minemind.core.history.HistoryEntry
import com.kim.minemind.core.history.HistoryStack

import android.util.Log
import com.kim.minemind.core.board.AppliedMove
import com.kim.minemind.shared.ConflictDelta
import com.kim.minemind.shared.ConflictList

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
        solver.clear()
        analyzer.clear()
        history.clear()
        board = Board(rows, cols, mines, seed = 0)
        _uiState.value = GameUiState(
            rows = rows,
            cols = cols,
            mines = mines,
            moves = 0,
            cells = buildCells(board)
        )
    }
    fun handleTopMenu(action: TopMenuAction) {
        if (action == TopMenuAction.NEW) {
            newGame(rows=25, cols= 25, mines=150)
        }
    }

    fun setTapMode(m: TapMode) {
        _uiState.update { it.copy(tapMode = m) }
    }

    fun dispatch(action: Action, gid: Int) {
        val beforeMoves = _uiState.value.moves

        val cs = board.apply(action, gid) // <-- TRUTH ONLY
        Log.d(TAG, "cs = $cs")
        if (cs.empty) return
        history.push(
            HistoryEntry(Move(gid, action, MoveKind.USER),
                cs,
                beforeMoves))

        val overlay = if (_uiState.value.isEnumerate) analyzer.analyze(board) else AnalyzerOverlay()
        val conflictDelta: ConflictDelta = if (_uiState.value.isEnumerate) board.conflictsByGid(cs.getAllGid()) else ConflictDelta()
        val conflictBoard = _uiState.value.conflictBoard.applyDelta(conflictDelta)

        Log.d(TAG, "overlay = $overlay")
        Log.d(TAG, "overlay.conflictProbs = ${overlay.conflictProbs}")
        Log.d(TAG, "conflictBoard = ${conflictBoard}")

        _uiState.update { s ->
            s.copy(
                moves = beforeMoves + 1,
                gameOver = cs.gameOver,
                win = cs.win,
                cells = patchCells(
                    base = s.cells,
                    cs = cs,
                    board = board,
                    overlay = overlay,
                    conflictsBoard = conflictBoard,
                    clearConflicts = conflictDelta.removes
                ),
                ruleList = overlay.ruleList,
                conflictBoard = conflictBoard,
                conflictProbs = overlay.conflictProbs
            )
        }
    }


    private fun patchCells(
        base: List<CellUI>,
        cs: ChangeSet,
        board: Board,
        overlay: AnalyzerOverlay,
        conflictsBoard: ConflictList = ConflictList(),
        clearConflicts: Set<Int> = emptySet()
    ): List<CellUI> {
        val updated = base.toMutableList()

        val changed = HashSet<Int>().apply {
            addAll(cs.revealed)
            addAll(cs.flagged)
            addAll(cs.exploded)

            addAll(conflictsBoard.keys)

            addAll(overlay.conflictProbs.keys)
            addAll(overlay.forcedOpens)
            addAll(overlay.forcedFlags)
            addAll(overlay.probabilities.keys)

            addAll(clearConflicts)
        }

        for (gid in changed) {
            val bc = board.cells[gid]

            updated[gid] = CellUI(
                gid = gid,

                isMine = bc.isMine,
                isRevealed = bc.isRevealed,
                isFlagged = bc.isFlagged,
                isExploded = bc.isExploded,
                adjacentMines = bc.adjacentMines,

                conflict = (gid in overlay.conflictProbs.keys) or (gid in conflictsBoard.keys),

                probability = overlay.probabilities.get(gid),

                forcedOpen = gid in (overlay.forcedOpens),
                forcedFlag = gid in (overlay.forcedFlags)
            )
        }

        return updated
    }

    fun step() {

    }
    fun auto() {

    }
    fun verify() {
        val v = _uiState.value.isVerify
        _uiState.update { s ->
            s.copy(
                isVerify = !v
            )
        }
    }

    fun enumerate() {
        val e = _uiState.value.isEnumerate
        if (!e) {
            val overlay = analyzer.analyze(board)

            val n = (0..((board.rows * board.cols)-1)).toSet()
            val conflictBoard: ConflictDelta = board.conflictsByGid(n)

            _uiState.update { s ->
                s.copy(
                    cells = patchCells(
                        base = s.cells,
                        cs = ChangeSet(),
                        board = board,
                        overlay = overlay
                    ),
                    isEnumerate = true,
                    ruleList = overlay.ruleList,
                    conflictBoard = conflictBoard.upserts,
                    conflictProbs = overlay.conflictProbs
                )
            }
        }
        else {
            _uiState.update { s -> s.copy( isEnumerate = false)}
        }
    }

    fun undo() {
        val entry = history.pop() ?: return

        board.undo(entry) // revert truth only

        var overlay = AnalyzerOverlay()
        var conflictDelta = ConflictDelta()

        if (_uiState.value.isEnumerate) {
            overlay = analyzer.analyze(board)
            val n = (0..((board.rows * board.cols)-1)).toSet()
            conflictDelta = board.conflictsByGid(n)
        }


        _uiState.update { s ->
            s.copy(
                moves = entry.moveCountBefore,
                gameOver = false,
                win = false,
                cells = buildCells(
                    b = board,
                    overlay = overlay,
                    conflictBoard = conflictDelta.upserts
                ),
                ruleList = overlay.ruleList,
                conflictBoard = conflictDelta.upserts,
                conflictProbs = overlay.conflictProbs
            )
        }
    }

    private fun buildCells(
        b: Board,
        overlay: AnalyzerOverlay = AnalyzerOverlay(),
        conflictBoard: ConflictList = ConflictList(),
        clearConflicts: Set<Int> = emptySet()
    ): List<CellUI> {
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

                    probability = overlay.probabilities.get(gid),

                    forcedOpen = gid in overlay.forcedOpens,
                    forcedFlag = gid in overlay.forcedFlags,

                    conflict = (gid in overlay.conflictProbs.keys) or (gid in conflictBoard.keys),
                )
            )
        }
        return out
    }


}
