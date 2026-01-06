package com.kim.minemind.analysis.rules

import com.kim.minemind.analysis.frontier.Component
import com.kim.minemind.shared.ReasonList
import java.util.BitSet

fun singlesRule (
    comp: Component,
    moves: RuleAggregator,
    stopAfterOne: Boolean,
) {
    for (constraint in comp.constraints) {
        val mask: BitSet = constraint.mask.clone() as BitSet

        val scopeSize = mask.cardinality()
        val remaining: Int = constraint.remaining

        if (remaining == 0) {
            var bit = mask.nextSetBit(0)
            while (bit >= 0) {
                val gid = comp.localToGlobal[bit]

                moves.addRule((mask.clone() as BitSet),
                            comp.localToGlobal,
                    Rule(gid=gid,
                        type=RuleType.SAFE,
                        reasons=ReasonList(initReasons = listOf("Singles: remaining == 0 are SAFE"))
                    )
                )

                if (stopAfterOne && moves.isNotEmpty()) {
                    return
                }
                bit = mask.nextSetBit(bit + 1)
            }
        }
        if (remaining == scopeSize) {
            var bit = mask.nextSetBit(0)
            while (bit >= 0) {
                val gid = comp.localToGlobal[bit]

                moves.addRule((mask.clone() as BitSet),
                    comp.localToGlobal,
                    Rule(gid=gid,
                        type=RuleType.MINE,
                        reasons=ReasonList(initReasons = listOf("Singles: remaining == |scope| are MINES"))
                    )
                )

                if (stopAfterOne && moves.isNotEmpty()) {
                    return
                }
                bit = mask.nextSetBit(bit + 1)
            }
        }
    }
}

