package com.kim.minemind.core

data class Move(
    val gid: Int, // global id
    val action: Action,
    val kind: MoveKind = MoveKind.USER,
    val reasons: List<String> = emptyList(),
    val score: Double? = null,
    val flagValue: Boolean? = null,
)
