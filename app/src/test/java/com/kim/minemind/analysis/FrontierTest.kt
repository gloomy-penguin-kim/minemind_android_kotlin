package com.kim.minemind.analysis

import com.kim.minemind.analysis.frontier.Component
import com.kim.minemind.analysis.frontier.Constraint
import com.kim.minemind.analysis.frontier.Frontier
import com.kim.minemind.core.board.Board
import org.junit.Assert.*
import org.junit.Test
import java.util.BitSet

class FrontierTest {

    @Test
    fun `frontier builds one component for one revealed number`() {
        // Arrange
        val board = Board(rows = 3, cols = 3, mines = 0 ,seed = 0)

        // Reveal center cell
        val center = 4
        board.cells[center].isRevealed = true
        board.cells[center].adjacentMines = 2

        val frontier = Frontier()

        // Act
        val components = frontier.build(board)

        // Assert
        assertEquals("Expected one component", 1, components.size)

        val comp = components[0]
        assertEquals("Expected 8 unknown neighbors", 8, comp.k)
        assertEquals(1, comp.constraints.size)

        val constraint = comp.constraints[0]
        assertEquals(2, constraint.remaining)
        assertEquals(8, constraint.mask.cardinality())
    }

    @Test
    fun `frontier builds one component for one revealed number with flag`() {
        // Arrange
        val board = Board(rows = 3, cols = 3, mines = 0 ,seed = 0)

        // Reveal center cell
        val center = 4
        board.cells[center].isRevealed = true
        board.cells[center].adjacentMines = 2
        board.cells[center+1].isFlagged = true

        val frontier = Frontier()

        // Act
        val components = frontier.build(board)

        // Assert
        assertEquals("Expected one component", 1, components.size)

        val comp = components[0]
        assertEquals("Expected 7 unknown neighbors", 7, comp.k)
        assertEquals(1, comp.constraints.size)

        val constraint = comp.constraints[0]
        assertEquals(1, constraint.remaining)
        assertEquals(7, constraint.mask.cardinality())
    }

    @Test
    fun `frontier splits independent regions into components`() {
        val board = Board(5, 5, 0, seed = 0)

        board.cells[6].apply {
            isRevealed = true
            adjacentMines = 1
        }
        board.cells[24].apply {
            isRevealed = true
            adjacentMines = 1
        }

        val frontier = Frontier()
        val comps = frontier.build(board)

        assertEquals(2, comps.size)
    }


    @Test
    fun `duplicate scopes are deduplicated`() {
        val board = Board(3, 3, 0, seed = 0)

        // Two revealed cells sharing same unknowns
        board.cells[1].apply {
            isRevealed = true
            adjacentMines = 1
        }
        board.cells[7].apply {
            isRevealed = true
            adjacentMines = 1
        }

        val frontier = Frontier()
        val comps = frontier.build(board)

        val comp = comps.single()
        assertEquals(2, comp.constraints.size)
    }

//    @Test
//    fun singles_marksScopeSafe_whenZeroRemainingMines() {
//        val board = Board(rows = 2, cols = 2, mines = 1, seed = 0)
//
//        // local indices 0,1,2 map to gids 0,1,2
//        val localToGlobal = intArrayOf(0, 1, 2, 3)
//
//        val mask = BitSet().apply { set(1); set(2) }
//
//        val comp = Component(
//            k = 3,
//            constraints = listOf(
//                Constraint.of(mask, remaining = 0),
//            ),
//            localToGlobal = localToGlobal
//        )
//
//        val frontier = Frontier()
//        frontier.build(board)
//    }

}
