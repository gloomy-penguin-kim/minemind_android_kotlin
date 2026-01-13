package com.kim.minemind.analysis.rules

import com.kim.minemind.analysis.frontier.Component
import com.kim.minemind.core.board.Board


class RuleEngine () {
    fun evaluate(board: Board, components: List<Component>, stopAfterOne: Boolean = false): RuleResult {

        val rules = RuleAggregator(board, stopAfterOne)

        for (component in components) {
            singlesRule(component, rules, stopAfterOne)
            if (stopAfterOne && rules.isNotEmpty()) break

            subsetsRule(component, rules, stopAfterOne)
            if (stopAfterOne && rules.isNotEmpty()) break

            equivalenceRule(component, rules, stopAfterOne)
            if (stopAfterOne && rules.isNotEmpty()) break
        }

        return RuleResult(
            forcedFlags = rules.forcedFlags,
            forcedOpens = rules.forcedOpens,
            ruleActionByGid = rules.actionByGid,

            conflictList = rules.getConflicts(),
            ruleList = rules.getRules()
        )
    }
}