package com.kim.minemind.analysis.rules

import com.kim.minemind.core.Action
import com.kim.minemind.core.board.Board

data class RuleResult (
    val forcedFlags: Set<Int>,
    val forcedOpens: Set<Int>,
    val ruleActionByGid: Map<Int, Action>,
    val conflicts: Set<Int>,
)