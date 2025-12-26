package com.kim.minemind.analysis.rules

import com.kim.minemind.analysis.frontier.Component
import com.kim.minemind.core.board.Board
import java.util.BitSet

class SubsetsRule (
    comp: Component,
    board: Board,
    //moves: MoveList
) {
    private fun isProperSubset(a: BitSet, b: BitSet): Boolean {
        // a ⊆ b  <=>  (a ∩ b) == a
        val inter = (a.clone() as BitSet).apply { and(b) } // inter = a & b
        if (inter != a) return false
        return a != b // proper subset (not equal)
    }

    private fun difference(b: BitSet, a: BitSet): BitSet {
        // b \ a
        return (b.clone() as BitSet).apply { andNot(a) }
    }

    fun foo(comp: Component, board: Board) {
        val constraints = comp.constraints
        val n = constraints.size

        for (i in constraints.indices) {
            for (j in constraints.indices) {
                if (i == j) continue

                val a = constraints[i].mask
                val b = constraints[j].mask
                val remA = constraints[i].remaining
                val remB = constraints[j].remaining

                // if (A ⊆ B) and A != B
                if (!isProperSubset(a, b)) continue

                // diff = B \ A
                val diff = difference(b, a)
                val diffSize = diff.cardinality()

                // if all in B\A are SAFE
                if (remA == remB) {
                    val reasons = listOf(
                        "Subset: A⊆B, a==b -> B\\A SAFE",
                        "${a} is a subset of",
                        "${b}"
                    )
                    processMovesForMask(diff, Action.OPEN, reasons)
                    if (stopAfterOne && movesCount() > 1) return
                }
                // all in B\A are MINES
                else if (remB - remA == diffSize) {
                    val reasons = listOf(
                        "Subset: A⊆B, b-a==|B\\A| -> B\\A MINES",
                        "${a} is a subset of",
                        "${b}"
                    )





                    processMovesForMask(diff, Action.FLAG, reasons)
                    if (stopAfterOne && movesCount() > 1) return
                }
            }
        }


    }


}