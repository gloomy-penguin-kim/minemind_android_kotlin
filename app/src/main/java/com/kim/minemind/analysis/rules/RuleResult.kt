package com.kim.minemind.analysis.rules

import com.kim.minemind.shared.Move
import com.kim.minemind.core.Action

data class RuleResult (
    val forcedFlags: Set<Int>,
    val forcedOpens: Set<Int>,
    val ruleActionByGid: Map<Int, Action>,
    val conflictsGid: Set<Int>,

    val conflicts: Map<Int, MutableSet<String>> = LinkedHashMap(),
    val rules: Map<Int, Move> = LinkedHashMap()
)
