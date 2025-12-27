package com.kim.minemind.analysis.analyzer

import com.kim.minemind.core.Action

data class CellOverlay(
    val gid: Int,
    val probability: Float? = null,     // 0.0..1.0
    val ruleAction: Action? = null,     // FLAG or OPEN when forced/suggested
    val conflict: Boolean = false,      // highlight inconsistencies
    val ruleTag: String? = null         // optional: “single”, “subset”, etc.
)

