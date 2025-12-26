package com.kim.minemind.analysis.rules

import com.kim.minemind.analysis.frontier.Component
import com.kim.minemind.core.Action
import com.kim.minemind.core.MoveKind
import com.kim.minemind.core.board.Board
import java.util.BitSet

fun singlesRule (
    comp: Component,
    board: Board,
    moves: MoveList,
) {
    for (constraint in comp.constraints) {
        val mask: BitSet = constraint.mask.clone() as BitSet

        val scopeSize = mask.cardinality()
        val remaining: Int = constraint.remaining

        if (remaining == 0) {
            for (gid in comp.localToGlobal) {
                board.cells[gid].probability = 0.0f
            }
        }
        if (remaining == scopeSize) {
            for (gid in comp.localToGlobal) {
                board.cells[gid].probability = 100.0f
            }
        }
    }
}

