package com.kim.minemind.ui.state

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.kim.minemind.core.*
import com.kim.minemind.analysis.Solver
import com.kim.minemind.analysis.enumeration.ProbabilityEngine
import com.kim.minemind.analysis.rules.Move
import com.kim.minemind.core.board.Board
import com.kim.minemind.core.board.Cell
import com.kim.minemind.core.history.ChangeSet
import com.kim.minemind.core.history.HistoryEntry
import com.kim.minemind.core.history.HistoryStack

class GameViewModel : ViewModel() {

    private var board: Board = Board(rows = 9, cols = 9, mines = 10, seed = 0)
    private var solver: Solver = Solver()

    private var probabilityEngine = ProbabilityEngine(maxK = 100, cacheCapacity = 256)

    private val history = HistoryStack()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    private fun idx(r: Int, c: Int, cols: Int) = r * cols + c



    init {
        newGame(rows=9, cols= 9, mines=10)
    }

    fun newGame(rows: Int, cols: Int, mines: Int) {
        board = Board(rows, cols, mines, seed = 0)
        solver.clear()
        probabilityEngine.clear()
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

        val csBoard = board.apply(action, gid)
        if (csBoard.empty) return

        val components = solver.buildFrontier(board)
        val probs = probabilityEngine.computeProbabilities(board, components) // Map<Int, Float?>

        // Create a ChangeSet that only represents probability updates
        val csProb = ChangeSet(probabilities = probs)

        // Merge into the “single truth for undo”
        val cs = csBoard.merged(csProb)

        history.push(HistoryEntry(Move(gid, action, MoveKind.USER), cs, beforeMoves))

        _uiState.update { s ->
            s.copy(
                moves = beforeMoves + 1,
                gameOver = cs.gameOver,
                win = cs.win,
                cells = patchCells(s.cells, cs, board) // now includes prob changes
            )
        }
    }



    private fun patchCells(
        base: List<CellUI>,
        cs: ChangeSet,
        board: Board
    ): List<CellUI> {
        val updated = base.toMutableList()

        val changed = HashSet<Int>().apply {
            addAll(cs.revealed)
            addAll(cs.flagged)
            addAll(cs.probabilities.keys)
        }

        for (gid in changed) {
            val bc = board.cells[gid]
            val r = gid / board.cols
            val c = gid % board.cols
            // val p = cs.probabilities[gid]

            val p = if (cs.probabilities.containsKey(gid)) cs.probabilities[gid] else updated[gid].probability

            updated[gid] = CellUI(
                row = r,
                col = c,
                gid = gid,
                isMine = bc.isMine,
                isRevealed = bc.isRevealed,
                isFlagged = bc.isFlagged,
                isExploded = bc.isExploded,
                adjacentMines = bc.adjacentMines,
                probability = p
            )
        }

        return updated
    }


    fun undo() {
        val entry = history.pop() ?: return
        board.undo(entry)

        val components = solver.buildFrontier(board)
        val probs = probabilityEngine.computeProbabilities(board, components)

        _uiState.update { s ->
            val rebuilt = buildCells(board)
            s.copy(
                moves = entry.moveCountBefore,
                gameOver = false,
                win = false,
                cells = rebuilt.mapIndexed { gid, cell ->
                    cell.copy(probability = probs[gid] ?: cell.probability)
                }
            )
        }
    }


    private fun buildCells(b: Board): List<CellUI> {
        val out = ArrayList<CellUI>(b.rows * b.cols)
        for (gid in 0 until (b.rows * b.cols)) {
            val bc = b.cells[gid]
            val r = gid / b.cols
            val c = gid % b.cols
            out.add(
                CellUI(
                    row = r,
                    col = c,
                    gid = gid,
                    isMine = bc.isMine,
                    isRevealed = bc.isRevealed,
                    isFlagged = bc.isFlagged,
                    adjacentMines = bc.adjacentMines,
                    probability = bc.probability
                )
            )
        }
        return out
    }


}
