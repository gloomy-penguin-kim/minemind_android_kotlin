package com.kim.minemind.analysis.rules

import com.kim.minemind.analysis.frontier.Component
import com.kim.minemind.shared.MoveList
import com.kim.minemind.core.board.Board


class RuleEngine () {
    fun evaluate(board: Board, components: List<Component>, stopAfterOne: Boolean = false): RuleResult {

        val moves = MoveList(board, stopAfterOne)

        for (component in components) {
            singlesRule(component, moves, stopAfterOne)
            if (stopAfterOne && moves.isNotEmpty()) break

            subsetsRule(component, moves, stopAfterOne)
            if (stopAfterOne && moves.isNotEmpty()) break

            equivalenceRule(component, moves, stopAfterOne)
            if (stopAfterOne && moves.isNotEmpty()) break
        }


        return RuleResult(
            forcedFlags=moves.forcedFlags,
            forcedOpens=moves.forcedOpens,
            ruleActionByGid=moves.ruleActionByGid,
            conflictsGid=moves.conflictsGid,

            conflicts = moves.conflicts,
            rules = moves.moves)
    }
}