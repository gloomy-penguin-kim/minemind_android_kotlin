package com.kim.minemind.shared

import com.kim.minemind.shared.ReasonList
import com.kim.minemind.core.Action
import com.kim.minemind.core.MoveKind

data class Move(
    val gid: Int,
    val action: Action,
    val moveKind: MoveKind,
    var reasons: ReasonList = ReasonList()
)