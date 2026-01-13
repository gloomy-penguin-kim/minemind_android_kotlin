package com.kim.minemind.core.board

import android.util.Log
import com.kim.minemind.core.Action
import com.kim.minemind.core.history.ChangeSet
import com.kim.minemind.core.history.HistoryEntry
import com.kim.minemind.shared.ConflictDelta
import com.kim.minemind.shared.snapshot.BoardSnapshot
import com.kim.minemind.shared.snapshot.CellSnapshot


class Board(
    val rows: Int,
    val cols: Int,
    val mines: Int,
    val seed: Int,
) {

    var minesPlaced: Boolean = false
    private set
    var gameOver: Boolean = false
        private set
    var win: Boolean = false
        private set

    var cells: MutableList<Cell> = MutableList(rows * cols) {
        Cell(it / cols, it % cols, it)
    }
        private set

    var remainingSafe: Int = rows * cols - mines
        private set
    var explodedGid: Int = -1
        private set

    fun setMinesPlaced(minesPlaced: Boolean) {
        this@Board.minesPlaced = minesPlaced
    }
    fun setGameOver(gameOver: Boolean) {
        this@Board.gameOver = gameOver
    }
    fun setWin(win: Boolean) {
        this@Board.win = win
    }
    fun setRemainingSafe(remainingSafe: Int) {
        this@Board.remainingSafe = remainingSafe
    }
    fun setExplodedGid(explodedGid: Int) {
        this@Board.explodedGid = explodedGid
    }
    fun setCells(cells: List<CellSnapshot>) {
        for (cell in cells) {
            this@Board.cells[cell.gid] = Cell(cell.gid / cols, cell.gid % cols, cell.gid)
            this@Board.cells[cell.gid].isFlagged = cell.isFlagged
            this@Board.cells[cell.gid].isMine = cell.isMine
            this@Board.cells[cell.gid].isRevealed = cell.isRevealed
            this@Board.cells[cell.gid].isExploded = cell.isExploded
            this@Board.cells[cell.gid].adjacentMines = cell.adjacentMines
        }
    }

    companion object {
        private const val TAG = "core.board"
    }

    fun gid(r: Int, c: Int) = r * cols + c
    fun rc(gid: Int) = gid / cols to gid % cols

    fun cell(r: Int, c: Int) = cells[gid(r, c)]
    fun cell(gid: Int) = cells[gid]

    fun apply(action: Action?, gid: Int, flagValue: Boolean? = null): ChangeSet {
        if (gameOver) return ChangeSet()
        if (action == null) return ChangeSet()

        val remainingSafeBefore = remainingSafe

        if (action == Action.OPEN && !minesPlaced) {
            generateBoard(gid)
        }

        val c: Pair<Int, Int>  = rc(gid)
        Log.d(TAG, "row = ${c.first}, col = ${c.second}")
        Log.d(TAG, "cell = ${cells[gid]} at $gid")
        Log.d(TAG, "remainingSafeBefore = $remainingSafeBefore")

        val csDelta = when (action) {
            Action.OPEN -> revealCell(gid)
            Action.FLAG -> toggleFlag(gid, flagValue)
            Action.CHORD -> chord(gid)
            else -> return ChangeSet()
        }
        remainingSafe -= csDelta.revealed.size
        Log.d(TAG, "remainingSafe = $remainingSafe")

        Log.d(TAG, "csDelta = $csDelta")
        val csWin = checkWinCondition()
        Log.d(TAG, "csWin = $csWin")
        val csCombined = csDelta.merged(csWin)
        Log.d(TAG, "apply = $csCombined")


        gameOver = csCombined.gameOver
        win = csCombined.win

        return csCombined
    }

    fun numberOfFlaggedAndUnknownNeighbors(gid: Int): IntArray {
        var flaggedNeighbors = 0
        var unknownNeighbors = 0
        for (nGid in neighbors(gid)) {
            val neighbor = cells[nGid]

            if (neighbor.isFlagged or (neighbor.isRevealed and neighbor.isMine)) {
                flaggedNeighbors += 1
            }
            if (!neighbor.isFlagged && !neighbor.isRevealed && !neighbor.isExploded) {
                unknownNeighbors += 1
            }
        }
        return intArrayOf(flaggedNeighbors, unknownNeighbors)
    }

    fun conflictsByGid(gids: Set<Int>): ConflictDelta {
        if (gameOver) return ConflictDelta()
        if (!minesPlaced) return ConflictDelta()
        if (gids.isEmpty()) return ConflictDelta()

        val conflictDelta = ConflictDelta()

        Log.d(TAG, "conflictsByGid = $gids")

        for (gid in gids) {
            val cell = cells[gid]

            val checkNeighborsAndSelf = neighbors(gid).toMutableList()
            if (!cell.isFlagged)
                checkNeighborsAndSelf.add(gid)

            for (nGid in checkNeighborsAndSelf) {
                val nCell = cells[nGid]
                if (nCell.isRevealed && !nCell.isExploded && !nCell.isFlagged) {
                    val (flagged, unknown) = numberOfFlaggedAndUnknownNeighbors(nGid)
                    if (flagged > nCell.adjacentMines) {
                        conflictDelta.upserts.addConflict(nGid, "Too many flags in this scope")
                    }
                    else if (flagged + unknown < nCell.adjacentMines) {
                        conflictDelta.upserts.addConflict(nGid, "Flagged + unknown < mines in this scope.")
                    }
                    else {
                        conflictDelta.removes += nGid
                    }
                }
                else conflictDelta.removes += nGid
            }
        }
        Log.d(TAG, "conflictDelta = $conflictDelta")
        return conflictDelta
    }

    private fun revealCell(gid: Int): ChangeSet {
        if (cells[gid].isRevealed || cells[gid].isFlagged) return ChangeSet()

        if (cells[gid].isMine) {
            return loseCondition(gid)
        }
        val csFlood = floodReveal(gid)
        val cs = ChangeSet(revealed = setOf(gid))
        val csCombined = cs.merged(csFlood)
        Log.d(TAG, "revealCell = $csCombined")
        cells[gid].isRevealed = true
        return csCombined
    }

    private fun floodReveal(gid: Int): ChangeSet {
        val revealedSet = mutableSetOf<Int>()
        val stack = ArrayDeque<Int>()
        stack.addLast(gid)

        Log.d(TAG, "floodReveal = $stack")

        while (stack.isNotEmpty()) {
            val gid = stack.removeLast()
            val cell = cells[gid]

            if (cell.isRevealed) continue
            if (cell.isFlagged) continue
            if (cell.isMine) continue
            if (cell.isExploded) continue

            Log.d(TAG, "floodReveal = $cell")

            cells[gid].isRevealed = true
            revealedSet.add(gid)

            if (cell.adjacentMines == 0) {
                for (nGid in neighbors(gid)) {
                    if (!cells[nGid].isRevealed &&
                        !cells[nGid].isFlagged &&
                        !cells[nGid].isMine &&
                        !cells[nGid].isExploded) {
                        stack.addLast(nGid)
                    }
                }
            }
        }
        return ChangeSet(revealed = revealedSet)
    }

    private fun loseCondition(gid:Int): ChangeSet {
        val exploded = mutableSetOf<Int>()
        for (cell in cells) {
            if (cell.isMine) {
                cell.isExploded = true
                exploded.add(cell.gid)
            }
        }
        cells[gid].isExplodedGid = true
        return ChangeSet(
            exploded = exploded,
            gameOver = true,
            win = false
        )
    }

    private fun checkWinCondition(): ChangeSet {
        if (gameOver) return ChangeSet()
        val newlyFlagged = mutableSetOf<Int>()

        Log.d(TAG, "remainingSafe = $remainingSafe")
        Log.d(TAG, "gameOver = $gameOver")
        if ((remainingSafe <= 0) and !gameOver) {
            for (cell in cells) {
                if (!cell.isRevealed && !cell.isFlagged) {
                    cell.isFlagged = true
                    newlyFlagged.add(cell.gid)
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

    private fun toggleFlag(gid: Int, flagValue: Boolean?): ChangeSet {
        if (cells[gid].isRevealed or !minesPlaced) return ChangeSet()
        val before = cells[gid].isFlagged
        val after = flagValue ?: !before
        if (before == after) return ChangeSet()
        cells[gid].isFlagged = after
        return ChangeSet(flagged = setOf(gid))
    }

    private fun chord(gid: Int): ChangeSet {
        if (!cells[gid].isRevealed and !cells[gid].isExploded) return ChangeSet()

        val mineCount = cells[gid].adjacentMines
        var flagCount = 0
        for (nGid in neighbors(gid)) {
            if (cells[nGid].isFlagged or (cells[nGid].isRevealed and cells[nGid].isMine)) {
                flagCount += 1
            }
        }
        Log.d(TAG, "chord = $mineCount, $flagCount")
        val revealed: MutableSet<Int> = mutableSetOf()
        var cs = ChangeSet()
        if (mineCount == flagCount) {
            for (nGid in neighbors(gid)) {
                if (!cells[nGid].isRevealed and !cells[nGid].isFlagged) {
                    val csReveal = revealCell(nGid)
                    cs = cs.merged(csReveal)
                    revealed.add(nGid)
                }
            }
        }
        Log.d(TAG, "chord = $revealed")
        Log.d(TAG, "cs = $cs")
        return cs
    }

    fun undo(entry: HistoryEntry) {
        entry.changes.revealed.forEach { gid -> cells[gid].isRevealed = !cells[gid].isRevealed }
        entry.changes.flagged.forEach { gid -> cells[gid].isFlagged = !cells[gid].isFlagged }
        entry.changes.exploded.forEach { gid -> cells[gid].isExploded = !cells[gid].isExploded }
        remainingSafe = entry.remainingSafeBefore
        gameOver = false
        win = false
    }

    fun isRevealed(gid: Int): Boolean = cells[gid].isRevealed
    fun isRevealedGid(gid: Int): Boolean {
        return cells[gid].isRevealed
    }
    fun isFlagged(gid: Int): Boolean = cells[gid].isFlagged
    fun isFlaggedGid(gid: Int): Boolean {
        return cells[gid].isFlagged
    }

    fun isMine(gid: Int): Boolean = cells[gid].isMine
    fun adjMines(gid: Int): Int = cells[gid].adjacentMines


    fun generateBoard(firstClickGid: Int) {
        Log.d(TAG, "generateBoard, $firstClickGid  $mines")

        val rng = RNG(seed, rows, cols, mines, firstClickGid)

        val forbidden: MutableSet<Int> = mutableSetOf(firstClickGid)
        neighbors(firstClickGid).forEach { forbidden.add(it) }
        var forbiddenList = forbidden.toMutableList()
        rng.shuffle(forbiddenList)
        Log.d(TAG, "forbiddenList = $forbiddenList")

        // Gather candidates (everything else)
        val candidates = mutableListOf<Int>()
        for (cell in cells) {
            if (forbiddenList.contains(cell.gid)) continue
            candidates.add(cell.gid)
        }

        if (mines > candidates.size) {
            throw IllegalArgumentException("Too many mines for given board size and safe zone")
        }

        // "Randomly" choose mine locations using seeded com.kim.minemind.core.board.RNG
        rng.shuffle(candidates)
        val mineCells = candidates.take(mines).toSet()

        mineCells.forEach { gid ->  cells[gid].isMine = true }

        for (cell in cells) {
            if (cell.isMine) {
                cell.adjacentMines = -1 // Mark mines as -1
            } else {
                var count = 0
                neighbors(cell.gid).forEach { nGid ->
                    if (cells[nGid].isMine) count++
                }
                cell.adjacentMines = count
            }
        }
        minesPlaced = true
    }

    fun neighbors(gid: Int): Sequence<Int> = sequence {
        val r = gid / cols
        val c = gid % cols
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = r + dr
                val nc = c + dc
                if (nr in 0 until rows && nc in 0 until cols) {
                    yield(nr * cols + nc)
                }
            }
        }
    }


    fun restore(s: BoardSnapshot) {
        require(rows == s.rows && cols == s.cols && mines == s.mines && seed == s.seed)

        minesPlaced = s.minesPlaced
        gameOver = s.gameOver
        win = s.win
        remainingSafe = s.remainingSafe
        explodedGid = s.explodedGid

        // rebuild cells list (or mutate existing)
        cells = s.cells
            .sortedBy { it.gid }
            .map { cs ->
                val (r, c) = cs.gid / cols to cs.gid % cols
                Cell(r, c, cs.gid).also { cell ->
                    cell.isMine = cs.isMine
                    cell.isRevealed = cs.isRevealed
                    cell.isFlagged = cs.isFlagged
                    cell.isExploded = cs.isExploded
                    cell.adjacentMines = cs.adjacentMines
                }
            }
            .toMutableList()
    }
}
