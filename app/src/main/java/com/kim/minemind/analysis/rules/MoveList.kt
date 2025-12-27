package com.kim.minemind.analysis.rules

import com.kim.minemind.core.Action
import com.kim.minemind.core.MoveKind
import com.kim.minemind.core.board.Board
import java.util.BitSet


class MoveList(
    private val board: Board,
    private val stopAfterOne: Boolean = false
) {

    // gid -> move
    private val moves: MutableMap<Int, Move> = LinkedHashMap()

    // gid -> set of reasons that conflicted at this gid
    private val conflicts: MutableMap<Int, MutableSet<String>> = LinkedHashMap()

    val forcedFlags: MutableSet<Int> = HashSet()
    val forcedOpens: MutableSet<Int> = HashSet()
    val conflictsGids: MutableSet<Int> = HashSet()
    val ruleActionByGid: MutableMap<Int, Action> = LinkedHashMap()

    fun addMove(mask: BitSet, localToGlobal: IntArray, move: Move) {
        if (stopAfterOne && moves.isNotEmpty()) return

        val gid = move.gid
        val existing = moves[gid]

        if (board.cells[gid].isFlagged or board.cells[gid].isRevealed) return

        if (gid in conflictsGids) {
            addConflict(gid, move)
            return
        }

        // this is a new gid and not found in the conflicts or
        // in the
        if (existing == null) {
            if (move.action == Action.FLAG) {
                if (board.cells[gid].isMine) {
                    moves[gid] = move
                    forcedFlags.add(gid)
                }
                else  {
                    addConflict(gid, move)
                }
            }
            else if (move.action == Action.OPEN) {
                if (!board.cells[gid].isMine) {
                    moves[gid] = move
                    forcedOpens.add(gid)
                }
                else  {
                    addConflict(gid, move)
                }
            }
            ruleActionByGid[gid] = move.action
            return
        }

        if (existing.action == move.action) {
            if (existing.reasons != move.reasons) {
                val reasons: MutableList<String> = existing.reasons.toMutableList()
                reasons.addAll(move.reasons)
                moves[gid] = Move(existing.gid, existing.action, existing.moveKind, reasons)
            }
            return
        }

//        // conflict found...!
//        conflictsGids.add(gid)
//
//        if (gid in ruleActionByGid) {
//            ruleActionByGid.remove(gid)
//        }
//        if (gid in forcedOpens) {
//            forcedOpens.remove(gid)
//        }
//        if (gid in forcedFlags) {
//            forcedFlags.remove(gid)
//        }
//
//        // Conflict: record both reasons and remove the move from moves
        val bucket = conflicts.getOrPut(gid) { LinkedHashSet() }

        existing.reasons.firstOrNull()?.let { bucket.add(it) }
        move.reasons.firstOrNull()?.let { bucket.add(it) }

        // add the entire scope to the conflictsGids array so they can be
        // highlighted in the front-end
        addConflicts(mask, localToGlobal, listOf("Other conflicts found in scope"))

        moves.remove(gid)
    }

    fun addConflict(gid: Int, move: Move) {
        if (board.cells[gid].isFlagged or board.cells[gid].isRevealed) return

        val conflict = conflicts.getOrPut(gid) { LinkedHashSet() }
        conflict.addAll(move.reasons)
        conflictsGids.add(gid)
        if (gid in ruleActionByGid) {
            ruleActionByGid.remove(gid)
        }
        if (gid in forcedOpens) {
            forcedOpens.remove(gid)
        }
        if (gid in forcedFlags) {
            forcedFlags.remove(gid)
        }
    }

    fun addConflicts(mask: BitSet, localToGlobal: IntArray, reasons: List<String>) {
        var bit = mask.nextSetBit(0)
        while (bit >= 0) {
            val gid = localToGlobal[bit]
            addConflict(gid, Move(gid, Action.INVALID, MoveKind.RULE, reasons))
            bit = mask.nextSetBit(bit + 1)
        }
    }

    fun isEmpty(): Boolean {
        return moves.isEmpty()
    }

    fun isNotEmpty(): Boolean {
        return moves.isNotEmpty()
    }
}
