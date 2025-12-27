package com.kim.minemind.analysis.rules

import com.kim.minemind.core.Action
import com.kim.minemind.core.MoveKind

data class Move(
    val gid: Int,
    val action: Action,
    val moveKind: MoveKind,
    var reasons: List<String> = emptyList()
)

//data class Move(
//    val gid: Int, // global id
//    val action: Action,
//    val kind: MoveKind = MoveKind.USER,
//    val reasons: List<String> = emptyList(),
//    val score: Double? = null,
//    val flagValue: Boolean? = null,
//)