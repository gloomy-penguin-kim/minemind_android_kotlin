package com.kim.minemind.analysis

import com.kim.minemind.analysis.frontier.Component
import com.kim.minemind.analysis.frontier.Constraint
import com.kim.minemind.shared.MoveList
import com.kim.minemind.analysis.rules.equivalenceRule
import com.kim.minemind.analysis.rules.singlesRule
import com.kim.minemind.analysis.rules.subsetsRule
import com.kim.minemind.core.board.Board
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.BitSet

class RulesTest {

    @Test
    fun equivalenceRule_marksConflicts_whenEqualMaskDifferentRemaining() {
        val board = Board(rows = 3, cols = 3, mines = 0, seed = 0)

        // local indices 0,1,2 map to gids 0,1,2
        val localToGlobal = intArrayOf(0, 1, 2)

        val m = BitSet().apply { set(0); set(1); set(2) }

        val comp = Component(
            k = 3,
            constraints = listOf(
                Constraint.of(m, remaining = 1),
                Constraint.of(m, remaining = 2),
            ),
            localToGlobal = localToGlobal
        )

        val moves = MoveList(board, stopAfterOne = false)

        equivalenceRule(comp, moves, stopAfterOne = false)

        // all gids in mask should be marked conflict-highlight
        assertTrue(moves.conflictsGids.containsAll(listOf(0, 1, 2)))
    }

    @Test
    fun singles_marksScopeSafe_whenZeroRemaining() {
        val board = Board(rows = 2, cols = 2, mines = 1, seed = 0)

        // local indices 0,1,2 map to gids 0,1,2
        val localToGlobal = intArrayOf(0, 1, 2, 3)

        val m = BitSet().apply { set(1); set(2) }

        val comp = Component(
            k = 3,
            constraints = listOf(
                Constraint.of(m, remaining = 0),
            ),
            localToGlobal = localToGlobal
        )

         val moves = MoveList(board, stopAfterOne = false)
        singlesRule(comp, moves, stopAfterOne = false)

        assertTrue(moves.forcedOpens.size == 2)
        assertTrue(1 in moves.forcedOpens)
        assertTrue(2 in moves.forcedOpens)
        assertTrue(moves.conflictsGids.isEmpty())
    }

    @Test
    fun singles_marksScopeMines_whenRemainingEqualsScope() {
        val board = Board(rows = 2, cols = 2, mines = 1, seed = 0)

        // local indices 0,1,2 map to gids 0,1,2
        val localToGlobal = intArrayOf(0, 1, 2, 3)

        val m = BitSet().apply { set(0); set(3) }

        val comp = Component(
            k = 2,
            constraints = listOf(
                Constraint.of(m, remaining = 2),
            ),
            localToGlobal = localToGlobal
        )

        val moves = MoveList(board, stopAfterOne = false)
        singlesRule(comp, moves, stopAfterOne = false)

        assertTrue(moves.forcedFlags.size == 2)
        assertTrue(0 in moves.forcedFlags)
        assertTrue(3 in moves.forcedFlags)
        assertTrue(moves.conflictsGids.isEmpty())
    }

    @Test
    fun subsets_ASubB_SameRemaining_MarksDiff_Safe() {
        val board = Board(rows = 2, cols = 2, mines = 0, seed = 0)

        // local indices 0,1,2 map to gids 0,1,2
        val localToGlobal = intArrayOf(0, 1, 2, 3)

        val m1 = BitSet().apply { set(0); set(1) }
        val m2 = BitSet().apply { set(0); set(1); set(2) }

        val comp = Component(
            k = 3,
            constraints = listOf(
                Constraint.of(m1, remaining = 2),
                Constraint.of(m2, remaining = 2),
            ),
            localToGlobal = localToGlobal
        )

        val moves = MoveList(board, stopAfterOne = false)
        subsetsRule(comp, moves, stopAfterOne = false)

        assertTrue(moves.forcedOpens.size == 1)
        assertTrue(2 in moves.forcedOpens)
        assertTrue(moves.conflictsGids.isEmpty())
    }

    @Test
    fun subsets_ASubB_RemainingDiff_Mines() {
        val board = Board(rows = 2, cols = 2, mines = 0, seed = 0)

        // local indices 0,1,2 map to gids 0,1,2
        val localToGlobal = intArrayOf(0, 1, 2, 3)

        val m1 = BitSet().apply { set(0); set(1) }
        val m2 = BitSet().apply { set(0); set(1); set(2) }

        val comp = Component(
            k = 3,
            constraints = listOf(
                Constraint.of(m1, remaining = 1),
                Constraint.of(m2, remaining = 2),
            ),
            localToGlobal = localToGlobal
        )

        val moves = MoveList(board, stopAfterOne = false)
        subsetsRule(comp, moves, stopAfterOne = false)

        assertTrue(moves.forcedFlags.size == 1)
        assertTrue(2 in moves.forcedFlags)
        assertTrue(moves.conflictsGids.isEmpty())
    }

    @Test
    fun subsets_StopAfterOne() {
        val board = Board(rows = 2, cols = 2, mines = 0, seed = 0)

        // local indices 0,1,2 map to gids 0,1,2
        val localToGlobal = intArrayOf(0, 1, 2, 3)

        val m1 = BitSet().apply { set(0); set(1) }
        val m2 = BitSet().apply { set(0); set(1); set(2) }

        val comp = Component(
            k = 3,
            constraints = listOf(
                Constraint.of(m1, remaining = 1),
                Constraint.of(m2, remaining = 2),
            ),
            localToGlobal = localToGlobal
        )

        val moves = MoveList(board, stopAfterOne = true)
        subsetsRule(comp, moves, stopAfterOne = true)

        assertTrue(moves.forcedFlags.size == 1)
        assertTrue(2 in moves.forcedFlags)
        assertTrue(moves.conflictsGids.isEmpty())
    }

    @Test
    fun singles_StopAfterOneTrue() {
        val board = Board(rows = 2, cols = 2, mines = 1, seed = 0)

        // local indices 0,1,2 map to gids 0,1,2
        val localToGlobal = intArrayOf(0, 1, 2, 3)

        val m = BitSet().apply { set(0); set(3) }

        val comp = Component(
            k = 2,
            constraints = listOf(
                Constraint.of(m, remaining = 2),
            ),
            localToGlobal = localToGlobal
        )

        val moves = MoveList(board, stopAfterOne = true)
        singlesRule(comp, moves, stopAfterOne = true)

        assertTrue(moves.forcedFlags.size == 1)
        assertTrue(0 in moves.forcedFlags)
        assertTrue(moves.conflictsGids.isEmpty())
    }
}