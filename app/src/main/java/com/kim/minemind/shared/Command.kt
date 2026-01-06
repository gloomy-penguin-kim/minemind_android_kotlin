package com.kim.minemind.shared

import com.kim.minemind.core.Action
import com.kim.minemind.core.MoveKind


data class Command(
    val action: Action,
    val gid: Int? = null,
    val flagValue: Boolean? = null,
)


//data class Move(
//    val gid: Int,
//    val action: Action,
//    val moveKind: MoveKind,
//    val flagValue: Boolean? = null,
//    var reasons: ReasonList = ReasonList()
//)

fun Command.toSnapshot(): CommandSnapshot = CommandSnapshot(
    action = this.action,
    gid = this.gid,
    flagValue = this.flagValue
)

data class CommandSnapshot(
    val action: Action,
    val gid: Int?,
    val flagValue: Boolean? = null,
)

fun CommandSnapshot.toMove(): Command {
    // adjust to your Move constructor
    return Command(
        action = action,
        gid = gid,
        flagValue = flagValue
    )
}

