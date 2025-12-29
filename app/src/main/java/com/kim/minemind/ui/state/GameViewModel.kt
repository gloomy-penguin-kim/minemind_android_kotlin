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
import kotlinx.coroutines.flow.getAndUpdate

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

        val truthDelta = board.apply(action, gid) // <-- TRUTH ONLY
        Log.d(TAG, "truthDelta = $truthDelta")
        if (truthDelta.changeSet.empty) return

        history.push(
            HistoryEntry(Move(gid, action, MoveKind.USER),
                                truthDelta.changeSet,
                                beforeMoves))

        val overlay = if (_uiState.value.isEnumerate) analyzer.analyze(board) else AnalyzerOverlay()

        overlay.conflicts.plus(truthDelta.conflicts)

        Log.d(TAG, "overlay = $overlay")
        Log.d(TAG, "forcedFlags = ${overlay.forcedFlags}")
        Log.d(TAG, "forcedOpens = ${overlay.forcedOpens}")
        Log.d(TAG, "conflicts = ${overlay.conflictsGid}")
        Log.d(TAG, "probs = ${overlay.probabilities}")

        _uiState.update { s ->
            s.copy(
                moves = beforeMoves + 1,
                gameOver = truthDelta.changeSet.gameOver,
                win = truthDelta.changeSet.win,
                cells = patchCells(
                    base = s.cells,
                    cs = truthDelta.changeSet,
                    board = board,
                    overlay = overlay
                ),
                overlay = overlay // <-- new overlay
            )
        }
    }


    private fun patchCells(
        base: List<CellUI>,
        cs: ChangeSet,
        board: Board,
        overlay: AnalyzerOverlay? = null
    ): List<CellUI> {
        val updated = base.toMutableList()

        val changed = HashSet<Int>().apply {
            addAll(cs.revealed)
            addAll(cs.flagged)
            addAll(cs.exploded)

            if (overlay != null) {
                // overlay-driven changes
                addAll(overlay.forcedOpens)
                addAll(overlay.forcedFlags)
                addAll(overlay.conflictsGid)
                addAll(overlay.probabilities.keys)
            }
        }

        // for (gid in changed) {
            for (gid in changed) {
                val bc = board.cells[gid]

                updated[gid] = CellUI(
                    gid = gid,

                    isMine = bc.isMine,
                    isRevealed = bc.isRevealed,
                    isFlagged = bc.isFlagged,
                    isExploded = bc.isExploded,
                    adjacentMines = bc.adjacentMines,

                    probability = overlay?.probabilities?.get(gid),

                    forcedOpen = gid in (overlay?.forcedOpens ?: emptySet()),
                    forcedFlag = gid in (overlay?.forcedFlags ?: emptySet()),
                    conflict = gid in (overlay?.conflictsGid ?: emptySet()),
                )
            }

        // }

        return updated
    }

    fun handleInfo(gid: Int) {

    }

    fun step() {
        val overlay = _uiState.value.overlay ?: analyzer.analyze(board, stopAfterOne = true)


        Log.d(TAG, "overlay = $overlay")
        Log.d(TAG, "forcedFlags = ${overlay.forcedFlags}")
        Log.d(TAG, "forcedOpens = ${overlay.forcedOpens}")
        Log.d(TAG, "probs = ${overlay.probabilities}")
    }
    fun auto() {
        val overlay = _uiState.value.overlay ?: analyzer.analyze(board, stopAfterOne = true)


        Log.d(TAG, "overlay = $overlay")
        Log.d(TAG, "forcedFlags = ${overlay.forcedFlags}")
        Log.d(TAG, "forcedOpens = ${overlay.forcedOpens}")
        Log.d(TAG, "probs = ${overlay.probabilities}")
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
            _uiState.update { s ->
                s.copy(
                    cells = patchCells(
                        base = s.cells,
                        cs = ChangeSet(),
                        board = board,
                        overlay = overlay
                    ),
                    isEnumerate = true,
                    overlay = overlay
                )
            }
        }
        else {
            _uiState.update { s -> s.copy( isEnumerate = false)}
        }
    }

    fun undo() {
        val entry = history.pop() ?: return
        val appliedMove = board.undo(entry) // revert truth only
        val overlay = analyzer.analyze(board)
        overlay.conflicts.plus(appliedMove.conflicts)

        Log.d(TAG, "overlay = $overlay")
        Log.d(TAG, "forcedFlags = ${overlay.forcedFlags}")
        Log.d(TAG, "forcedOpens = ${overlay.forcedOpens}")
        Log.d(TAG, "conflicts = ${overlay.conflictsGid}")
        Log.d(TAG, "probs = ${overlay.probabilities}")

        _uiState.update { s ->
            s.copy(
                moves = entry.moveCountBefore,
                gameOver = false,
                win = false,
                cells = buildCells(board, overlay),
                overlay = overlay
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

                    forcedOpen = gid in (overlay?.forcedOpens ?: emptySet()),
                    forcedFlag = gid in (overlay?.forcedFlags ?: emptySet()),
                    conflict = gid in (overlay?.conflictsGid ?: emptySet()),
                )
            )
        }
        return out
    }


}
