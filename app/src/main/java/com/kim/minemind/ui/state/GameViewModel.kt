package com.kim.minemind.ui.state

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.kim.minemind.core.*
import com.kim.minemind.analysis.Solver
import com.kim.minemind.analysis.enumeration.ProbabilityEngine
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

    fun dispatch(action: Action, r: Int, c: Int) {
        val beforeMoves = _uiState.value.moves
        val move = Move(r = r, c = c, action = action, kind = MoveKind.USER)

        val cs = board.apply(action, r, c)
        if (cs.empty) return

        history.push(HistoryEntry(move, cs, moveCountBefore = beforeMoves))

        val components = solver.buildFrontier(board)
        val probs = probabilityEngine.computeProbabilities(board, components)
        // probs is now a Map<Int, Flaot> for globalId and the float percentage

        _uiState.update { s ->
            s.copy(
                moves = beforeMoves + 1,
                gameOver = cs.gameOver,
                win = cs.win,
                cells = patchCells(s.cells, cs, board)
            )
        }
    }



    private fun patchCells(
        base: List<Cell>,
        cs: ChangeSet,
        board: Board
    ): List<Cell> {
        val cols = board.cols
        val updated = base.toMutableList()

        // Union of all coords we need to refresh in UI
        val changed = HashSet<Int>().apply {
            addAll(cs.revealed)
            addAll(cs.flagged)
            addAll(cs.probabilities.keys)
        }

        for (gid in changed) {
            val bc = board.cells[gid]
            val p = cs.probabilities[r to c] ?: bc.probability

            updated[idx(r, c, cols)] = Cell(
                row = r,
                col = c,
                isMine = bc.isMine,
                isRevealed = bc.isRevealed,
                isFlagged = bc.isFlagged,
                adjacentMines = bc.adjacentMines,
                probability = p
            )
        }

        return updated
    }

    fun undo() {
        val entry = history.pop() ?: return
        board.undo(entry)

        // You likely have (or can add) a ChangeSet returned from undo;
        // if not, easiest is: rebuild once for undo, or store undo changes in HistoryEntry.
        _uiState.update { s ->
            s.copy(
                moves = entry.moveCountBefore,
                gameOver = false,
                win = false,
                cells = buildCells(board) // OK for now; later patch incrementally too
            )
        }
    }


    private fun buildCells(b: Board): List<Cell> {
        val out = ArrayList<Cell>(b.rows * b.cols)
        for (r in 0 until b.rows) {
            for (c in 0 until b.cols) {
                out.add(
                    Cell(
                        row = r,
                        col = c,
                        isMine = b.cells[r][c].isMine,
                        isRevealed = b.cells[r][c].isRevealed,
                        isFlagged = b.cells[r][c].isFlagged,
                        adjacentMines = b.cells[r][c].adjacentMines,
                        probability = b.cells[r][c].probability
                    )
                )
            }
        }
        return out
    }


}
