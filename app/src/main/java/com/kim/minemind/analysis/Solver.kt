package com.kim.minemind.analysis

import com.kim.minemind.analysis.enumeration.ProbabilityEngine
import com.kim.minemind.analysis.frontier.Component
import com.kim.minemind.core.*
import com.kim.minemind.core.board.Board
import com.kim.minemind.analysis.frontier.Frontier
import com.kim.minemind.analysis.rules.Move

class Solver() {


    //    5) Solver (policy/orchestrator)
        //    Solver decides what to do next (especially for “step”).
            //    It calls Analyzer to get forced moves first.
            //    If forced moves exist → returns a Move (or list of moves)
            //    Else, uses probabilities to choose a best guess (lowest p).
        //    Solver should not mutate Board directly; it should request actions:
            //    sealed class Move {
            //        data class Open(val gid: Int): Move()
            //        data class Flag(val gid: Int): Move()
            //        object None: Move()
            //    }

//    Builds constraints/frontier from board
//    Runs deterministic rules (singles/subsets/equivalence/etc.)
//    Can produce:
//        suggested moves (for “Step” button)
//        forced sets (forcedFlags/forcedOpens)
//        conflict info (contradictions in constraints)

    val frontier: Frontier = Frontier()
    var components: List<Component> = emptyList()


    fun buildFrontier(board: Board): List<Component> {
        components = frontier.build(board)
        return components
    }

    fun clear() {
        components = emptyList()
    }


    fun hint(guess: Boolean = false): List<Move> = emptyList()
    fun step(guess: Boolean = false): List<Move> = emptyList()
    fun auto(guess: Boolean = false, limit: Int? = null): List<Move> = emptyList()
}
