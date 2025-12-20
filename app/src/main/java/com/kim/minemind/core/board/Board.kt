package com.kim.minemind.core.board

import android.util.Log
import com.kim.minemind.analysis.enumeration.ProbabilityEngine
import com.kim.minemind.core.Action
import com.kim.minemind.core.history.ChangeSet
import com.kim.minemind.core.history.HistoryEntry
import com.kim.minemind.analysis.Solver

class Board(
    val rows: Int,
    val cols: Int,
    val mines: Int,
    val seed: Int
) {
    var minesPlaced: Boolean = false
        private set

    var gameOver: Boolean = false
        private set

    var win: Boolean = false
        private set

    var cells: Array<Array<Cell>> = Array(rows) { Array(cols) { Cell(it, it) } }

    var remainingSafe: Int = rows * cols - mines


    companion object {
        private const val TAG = "core.board"
    }
    fun apply(action: Action, r: Int, c: Int, flagValue: Boolean? = null): ChangeSet {
        if (gameOver) return ChangeSet()

        if (action == Action.OPEN && !minesPlaced) {
            generateBoard(r to c)
        }
        Log.d(TAG, "row = $r, col = $c")
        Log.d(TAG, "cell = ${cells[r][c]}")

        val csDelta = when (action) {
            Action.OPEN -> revealCell(r, c)
            Action.FLAG -> toggleFlag(r, c, flagValue)
            Action.CHORD -> chord(r, c)
        }
        val csWin = checkWinCondition()
        val csCombined = csDelta.merged(csWin)
        Log.d(TAG, "apply = $csCombined")

        return csCombined
    }

    private fun revealCell(r: Int, c: Int): ChangeSet {
        if (cells[r][c].isRevealed || cells[r][c].isFlagged) return ChangeSet()

        if (cells[r][c].isMine) {
            return loseCondition()
        }
        val csFlood = floodReveal(r, c)
        val cs = ChangeSet(revealed = setOf(r to c))
        val csCombined = cs.merged(csFlood)
        Log.d(TAG, "revealCell = $csCombined")
        cells[r][c].isRevealed = true
        return csCombined
    }

    private fun floodReveal(r: Int, c: Int): ChangeSet {
        val revealedSet = mutableSetOf<Pair<Int, Int>>()
        val stack = ArrayDeque<Pair<Int, Int>>()
        stack.addLast(r to c)

        while (stack.isNotEmpty()) {
            val (cr, cc) = stack.removeLast()
            val cell = cells[cr][cc]

            Log.d(TAG, "floodReveal = $cell")

            if (cell.isRevealed) continue
            if (cell.isFlagged) continue
            if (cell.isMine) continue
            if (cell.isExploded) continue

            cells[cr][cc].isRevealed = true
            revealedSet.add(cr to cc)
            remainingSafe -= 1

            if (cell.adjacentMines == 0) {
                for ((nr, nc) in neighbors(cr, cc)) {
                    if (!cells[nr][nc].isRevealed && !cells[nr][nc].isFlagged) {
                        stack.addLast(nr to nc)
                    }
                }
            }
        }
        return ChangeSet(revealed = revealedSet)
    }


    private fun loseCondition(): ChangeSet {
        val mines = mutableSetOf<Pair<Int, Int>>()

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (cells[i][j].isMine) {
                    cells[i][j].isRevealed = true
                    mines.add(i to j)
                }
            }
        }

        return ChangeSet(
            revealed = mines,
            flagged = emptySet(),
            gameOver = true,
            win = false
        )
    }

    private fun checkWinCondition(): ChangeSet {
        val newlyFlagged = mutableSetOf<Pair<Int, Int>>()

        if (remainingSafe <= 0 && !gameOver) {
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    if (!cells[i][j].isRevealed && !cells[i][j].isFlagged) {
                        cells[i][j].isFlagged = true
                        newlyFlagged.add(i to j)
                    }
                }
            }
            win = true
            gameOver = true
        }

        return ChangeSet(
            revealed = emptySet(),
            flagged = newlyFlagged,
            gameOver = gameOver,
            win = win
        )
    }

    private fun toggleFlag(r: Int, c: Int, flagValue: Boolean?): ChangeSet {
        if (cells[r][c].isRevealed or !minesPlaced) return ChangeSet()
        val before = cells[r][c].isFlagged
        val after = flagValue ?: !before
        if (before == after) return ChangeSet()
        cells[r][c].isFlagged = after
        return ChangeSet(flagged = setOf(r to c))
    }

    private fun chord(r: Int, c: Int): ChangeSet {
        return ChangeSet()
    }

    fun undo(entry: HistoryEntry) {
        entry.changes.revealed.forEach { (rr, cc) -> cells[rr][cc].isRevealed = false }
        entry.changes.flagged.forEach { (rr, cc) -> cells[rr][cc].isFlagged = false }
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val key: Pair<Int, Int> = r to c
                if (key in entry.changes.probabilities)
                    cells[r][c].probability = entry.changes.probabilities[key]!!
                else
                    cells[r][c].probability = null
            }
        }
        gameOver = false
        win = false
    }

    fun isRevealed(r: Int, c: Int): Boolean = cells[r][c].isRevealed
    fun isRevealedGid(gid: Int): Boolean {
        val r = gid / cols
        val c = gid % cols
        return cells[r][c].isRevealed
    }
    fun isFlagged(r: Int, c: Int): Boolean = cells[r][c].isFlagged
    fun isFlaggedGid(gid: Int): Boolean {
        val r = gid / cols
        val c = gid % cols
        return cells[r][c].isFlagged
    }

    fun isMine(r: Int, c: Int): Boolean = cells[r][c].isMine
    fun adjMines(r: Int, c: Int): Int = cells[r][c].adjacentMines


    fun generateBoard(firstClick: Pair<Int, Int>) {
        Log.d(TAG, "generateBoard, $firstClick")

        val rng = RNG(seed, rows, cols, mines, firstClick)

        val r0 = firstClick.first
        val c0 = firstClick.second

        val forbidden = mutableSetOf(Pair(r0, c0))
        neighbors( r0, c0).forEach { forbidden.add(it) }
        val forbiddenList = forbidden.toMutableList()
        rng.shuffle(forbiddenList)
        forbiddenList.take(n)
        Log.d(TAG, "forbiddenList = $forbiddenList")

        // Gather candidates (everything else)
        val candidates = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (forbiddenList.contains(Pair(r, c))) continue
                candidates.add(Pair(r, c))
            }
        }

        if (mines > candidates.size) {
            throw IllegalArgumentException("Too many mines for given board size and safe zone")
        }

        // "Randomly" choose mine locations using seeded com.kim.minemind.core.board.RNG
        rng.shuffle(candidates)
        val mineCells = candidates.take(mines).toSet()

        mineCells.forEach { (r, c) ->  cells[r][c].isMine = true }

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (cells[r][c].isMine) {
                    cells[r][c].adjacentMines = -1 // Mark mines as -1
                } else {
                    var count = 0
                    neighbors(r, c).forEach { (nr, nc) ->
                        if (cells[nr][nc].isMine) count++
                    }
                    cells[r][c].adjacentMines = count
                }
            }
        }
        minesPlaced = true
    }

    fun neighbors(r: Int, c: Int): Sequence<Pair<Int, Int>> = sequence {
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = r + dr
                val nc = c + dc
                if (nr in 0 until rows && nc in 0 until cols) {
                    yield(nr to nc)
                }
            }
        }
    }

}