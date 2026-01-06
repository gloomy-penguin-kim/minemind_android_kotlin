package com.kim.minemind.analysis

import com.kim.minemind.analysis.rules.Rule
import com.kim.minemind.core.Action
import com.kim.minemind.shared.ConflictList

data class AnalyzerOverlay(
    val probabilities: Map<Int, Float?> = emptyMap(),  // gid -> p(mine)

    val ruleActions: Map<Int, Action?> = emptyMap(),   // gid -> why/what (optional)

    val forcedFlags: Set<Int> = emptySet(),            // mines proven by rules
    val forcedOpens: Set<Int> = emptySet(),             // safe cells proven by rules

    var conflictProbs: ConflictList = ConflictList(),

    val ruleList: Map<Int, Rule> = LinkedHashMap(),

    val isConsistent: Boolean = true
)