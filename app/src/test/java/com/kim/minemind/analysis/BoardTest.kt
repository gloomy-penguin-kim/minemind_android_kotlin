package com.kim.minemind.analysis

import com.kim.minemind.analysis.frontier.Frontier
import com.kim.minemind.core.board.Board
import org.junit.Assert.assertEquals
import org.junit.Test

import com.kim.minemind.core.Action
import com.kim.minemind.core.history.ChangeSet
import com.kim.minemind.shared.ConflictDelta
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue


class BoardTest {


    @Test
    fun `board has conflicts`() {
        // Arrange
        val board = Board(rows = 2, cols = 2, mines = 0 ,seed = 0)
        board.generateBoard(3)

        board.cells[0].isFlagged = true
        board.cells[1].isFlagged = true
        board.cells[3].adjacentMines = 1

        board.apply(Action.OPEN, 3)

        val conflictDelta: ConflictDelta = board.conflictsByGid(setOf(0,1,2,3))

        assert(conflictDelta.upserts.size == 1)
        assertTrue(conflictDelta.upserts.getConflicts().containsKey(3))
    }


    @Test
    fun `board has a chord success`() {
        // Arrange
        val board = Board(rows = 2, cols = 2, mines = 0 ,seed = 0)
        board.generateBoard(3)

        board.cells[0].isMine = true
        board.cells[0].isFlagged = true
        board.cells[1].adjacentMines = 1
        board.cells[2].adjacentMines = 1
        board.cells[3].adjacentMines = 1
        board.apply(Action.OPEN, 3)

        val cs: ChangeSet = board.apply(Action.CHORD, 3)
        assert(cs.revealed.size == 2)

        val conflictDelta: ConflictDelta = board.conflictsByGid(setOf(0,1,2,3))
        assert(conflictDelta.upserts.size == 0)
    }

    @Test
    fun `board has a chord failure`() {
        // Arrange
        val board = Board(rows = 2, cols = 2, mines = 0 ,seed = 0)
        board.generateBoard(3)

        board.cells[0].isMine = true
        board.cells[1].adjacentMines = 1
        board.cells[1].isFlagged = true
        board.cells[2].adjacentMines = 1
        board.cells[3].adjacentMines = 1
        board.apply(Action.OPEN, 3)

        val cs: ChangeSet = board.apply(Action.CHORD, 3)
        assert(cs.revealed.size == 1)
        assert(cs.exploded.size == 1)
        assertTrue(cs.gameOver)
        assertFalse(cs.win)

        val conflictDelta: ConflictDelta = board.conflictsByGid(setOf(0,1,2,3))
        assert(conflictDelta.upserts.size == 0)
    }

    @Test
    fun `board has a lose condition`() {
        // Arrange
        val board = Board(rows = 3, cols = 3, mines = 4 ,seed = 0)

        board.cells[0].isMine = true
        board.cells[4].isMine = true
        board.cells[6].isMine = true
        board.cells[8].isMine = true

        board.setMinesPlaced(true)

        val cs1: ChangeSet = board.apply(Action.FLAG, 0)
        assert(cs1.flagged.size == 1)
        assertTrue(cs1.revealed.isEmpty())
        assertTrue(cs1.exploded.isEmpty())

        val cs2: ChangeSet = board.apply(Action.OPEN, 8)
        assertTrue(cs2.revealed.isEmpty())
        assert(cs2.exploded.size == 4)
        assertTrue(cs2.gameOver)
        assertFalse(cs2.win)

        val conflictDelta: ConflictDelta = board.conflictsByGid(setOf(0,1,2,3))
        assert(conflictDelta.upserts.size == 0)
    }
}