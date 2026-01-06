package com.kim.minemind.analysis.rules

import com.kim.minemind.core.Action
import com.kim.minemind.shared.ReasonList

enum class RuleType { SAFE, MINE, UNKNOWN }

data class Rule (
    val gid: Int,
    val type: RuleType,
    val reasons: ReasonList = ReasonList()
) {
    fun toAction(): Action {
        return when (type) {
            RuleType.SAFE -> Action.OPEN
            RuleType.MINE -> Action.FLAG
            else -> Action.OPEN
        }
    }
}

