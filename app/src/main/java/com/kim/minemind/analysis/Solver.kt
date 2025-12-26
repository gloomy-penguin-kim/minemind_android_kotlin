package com.kim.minemind.analysis

import com.kim.minemind.analysis.enumeration.ProbabilityEngine
import com.kim.minemind.analysis.frontier.Component
import com.kim.minemind.core.*
import com.kim.minemind.core.board.Board
import com.kim.minemind.analysis.frontier.Frontier
import com.kim.minemind.analysis.rules.Move

class Solver() {

    val frontier: Frontier = Frontier()
    var components: List<Component> = emptyList()


    fun buildFrontier(board: Board): List<Component> {
        components = frontier.buildFrontier(board)
        return components
    }

    fun clear() {
        components = emptyList()
    }


    fun hint(guess: Boolean = false): List<Move> = emptyList()
    fun step(guess: Boolean = false): List<Move> = emptyList()
    fun auto(guess: Boolean = false, limit: Int? = null): List<Move> = emptyList()
}
