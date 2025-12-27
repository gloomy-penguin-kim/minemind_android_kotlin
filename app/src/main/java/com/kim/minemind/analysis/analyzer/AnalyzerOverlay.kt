package com.kim.minemind.analysis.analyzer

import com.kim.minemind.core.Action

data class AnalyzerOverlay(
    val probabilities: Map<Int, Float?> = emptyMap(),  // gid -> p(mine)
    val ruleActions: Map<Int, Action?> = emptyMap(),   // gid -> why/what (optional)
    val conflicts: Set<Int> = emptySet(),              // gids to highlight
    val forcedFlags: Set<Int> = emptySet(),            // mines proven by rules
    val forcedOpens: Set<Int> = emptySet()             // safe cells proven by rules
)