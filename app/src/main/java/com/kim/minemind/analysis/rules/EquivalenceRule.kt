package com.kim.minemind.analysis.rules

import com.kim.minemind.analysis.frontier.Component
import com.kim.minemind.core.Action
import com.kim.minemind.core.MoveKind
import java.util.BitSet


fun equivalenceRule(
    comp: Component,
    moves: MoveList,
    stopAfterOne: Boolean
) {
    fun isProperSubset(a: BitSet, b: BitSet): Boolean {
        // a ⊆ b  <=>  (a ∩ b) == a
        val inter = (a.clone() as BitSet).apply { and(b) }
        return inter == a && a != b
    }

    fun difference(b: BitSet, a: BitSet): BitSet {
        // b \ a
        return (b.clone() as BitSet).apply { andNot(a) }
    }

    fun enqueueMovesForMask(mask: BitSet, action: Action, reasons: List<String>) {
        var idx = mask.nextSetBit(0)
        while (idx >= 0) {
            val gid = comp.localToGlobal[idx]
            if (action == Action.FLAG) {
                moves.addMove(mask, comp.localToGlobal,
                    Move(gid, action, MoveKind.RULE, reasons)
                )
            }
            idx = mask.nextSetBit(idx + 1)
        }
    }

    val constraints = comp.constraints
    val n = constraints.size

    for (i in 0 until n) {
        val a = constraints[i].mask
        val remA = constraints[i].remaining
        if (a.isEmpty) continue

        for (j in i + 1 until n) {
            val b = constraints[j].mask
            val remB = constraints[j].remaining
            if (b.isEmpty) continue

            // (1) Exact scope equality contradiction check
            if (a == b && remA != remB) {
                moves.addConflicts(mask = a,
                    localToGlobal = comp.localToGlobal,
                    reasons = listOf(
                        "A == B but remaining differs",
                        "mask=${a} remA=$remA",
                        "mask=${b} remB=$remB"
                    )
                )

                // no early-stop here by default; you can if you want:
                 if (stopAfterOne) return
                continue
            }

            // (2) Proper subset + equal remaining => diff SAFE

            // A ⊂ B and rem(A) == rem(B) => (B\A) safe
            if (remA == remB && isProperSubset(a, b)) {
                val diff = difference(b, a)
                if (!diff.isEmpty) {
                    val reasons = listOf(
                        "Equivalence: A⊂B and rem(A)==rem(B) -> B\\A SAFE",
                        "A=$a rem=$remA is subset of ",
                        "B=$b rem=$remB"
                    )
                    enqueueMovesForMask(diff, Action.OPEN, reasons)
                    if (stopAfterOne && moves.isNotEmpty()) return
                }
            }

            // B ⊂ A and rem(A) == rem(B) => (A\B) safe
            if (remA == remB && isProperSubset(b, a)) {
                val diff = difference(a, b)
                if (!diff.isEmpty) {
                    val reasons = listOf(
                        "Equivalence: B⊂A and rem(A)==rem(B) -> A\\B SAFE",
                        "B=$b rem=$remB is subset of ",
                        "A=$a rem=$remA"
                    )
                    enqueueMovesForMask(diff, Action.OPEN, reasons)
                    if (stopAfterOne && moves.isNotEmpty()) return
                }
            }
        }
    }
}
