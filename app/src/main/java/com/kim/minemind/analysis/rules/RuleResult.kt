package com.kim.minemind.analysis.rules

import com.kim.minemind.shared.Move
import com.kim.minemind.core.Action
import com.kim.minemind.shared.ConflictList

data class RuleResult (
    val forcedFlags: Set<Int>,
    val forcedOpens: Set<Int>,
    val ruleActionByGid: Map<Int, Action>,

    val conflictList: ConflictList = ConflictList(),
    val ruleList: Map<Int, Move> = LinkedHashMap()
)
