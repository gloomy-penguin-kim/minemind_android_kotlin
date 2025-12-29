package com.kim.minemind.shared

import com.kim.minemind.shared.ReasonList
import com.kim.minemind.core.Action
import com.kim.minemind.core.board.Board
import java.util.BitSet

class MoveList(
    private val board: Board = Board(rows = 0, cols = 0, mines = 0, seed = 0),
    private val stopAfterOne: Boolean = false
) {

    // gid -> move
    val moves: MutableMap<Int, Move> = LinkedHashMap()

    // gid -> set of reasons that conflicted at this gid
    val conflicts: MutableMap<Int, ReasonList> = LinkedHashMap()

    val forcedFlags: MutableSet<Int> = HashSet()
    val forcedOpens: MutableSet<Int> = HashSet()
    val ruleActionByGid: MutableMap<Int, Action> = LinkedHashMap()

    fun addMove(mask: BitSet, localToGlobal: IntArray, move: Move) {
        if (stopAfterOne && moves.isNotEmpty()) return

        val gid = move.gid
        val existing = moves[gid]

        if (board.cells[gid].isFlagged or board.cells[gid].isRevealed) return

        if (gid in conflicts.keys) {
            addConflict(gid, move.reasons)
            return
        }

        // this is a new gid and not found in the conflicts or
        // in the
        if (existing == null) {
            if (move.action == Action.FLAG) {
                moves[gid] = move
                forcedFlags.add(gid)
            }
            else if (move.action == Action.OPEN) {
                moves[gid] = move
                forcedOpens.add(gid)
            }
            ruleActionByGid[gid] = move.action
            return
        }

        if (existing.action == move.action) {
            if (existing.reasons != move.reasons) {
//                val reasons: MutableList<String> = existing.reasons.toMutableList()
//                reasons.addAll(move.reasons)
//                moves[gid] = Move(existing.gid, existing.action, existing.moveKind, reasons)
                existing.reasons.addReasons(move.reasons)
            }
            return
        }


       // Conflict: record both reasons and remove the move from moves
        val conflictReasonList  = conflicts.getOrPut(gid) { ReasonList() }

        conflictReasonList.addReasons(existing.reasons)
        conflictReasonList.addReasons(move.reasons)


//        existing.reasons.forEach { r ->
//            if (!bucket.contains(r)) bucket.add(r)
//        }
//        move.reasons.forEach { r ->
//            if (!bucket.contains(r)) bucket.add(r)
//        }

        // add the entire scope to the conflictsGids array so they can be
        // highlighted in the front-end
        addConflicts(mask, localToGlobal, conflictReasonList)

        moves.remove(gid)
    }

    fun addConflict(gid: Int, reasonList: ReasonList) {
        if (board.cells[gid].isFlagged or board.cells[gid].isRevealed) return

        val conflict = conflicts.getOrPut(gid) { ReasonList() }
        conflict.addReasons(reasonList)

        if (ruleActionByGid.contains(gid)) {
            ruleActionByGid.remove(gid)
        }
        if (forcedOpens.contains(gid)) {
            forcedOpens.remove(gid)
        }
        if (forcedFlags.contains(gid)) {
            forcedFlags.remove(gid)
        }
        if (moves.contains(gid)) {
            moves.remove(gid)
        }
    }

    fun addConflicts(mask: BitSet, localToGlobal: IntArray, reasonList: ReasonList) {
        var bit = mask.nextSetBit(0)
        while (bit >= 0) {
            val gid = localToGlobal[bit]
            addConflict(gid, reasonList)
            bit = mask.nextSetBit(bit + 1)
        }
    }

    fun isEmpty(): Boolean {
        return moves.isEmpty()
    }

    fun isNotEmpty(): Boolean {
        return moves.isNotEmpty()
    }

    fun barf() {

    }
}