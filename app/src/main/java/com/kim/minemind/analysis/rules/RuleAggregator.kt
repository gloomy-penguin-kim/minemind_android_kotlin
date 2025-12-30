package com.kim.minemind.analysis.rules

import com.kim.minemind.core.Action
import com.kim.minemind.core.board.Board
import com.kim.minemind.shared.ConflictList
import com.kim.minemind.shared.Move
import java.util.BitSet
import kotlin.collections.MutableMap

import com.kim.minemind.shared.ReasonList

class RuleAggregator(
    private val board: Board = Board(rows = 0, cols = 0, mines = 0, seed = 0),
    private val stopAfterOne: Boolean = false
) {

    // gid -> move
    private val moves: MutableMap<Int, Move> = LinkedHashMap()

    private val conflicts: ConflictList = ConflictList()

    val forcedFlags: MutableSet<Int> = HashSet()
    val forcedOpens: MutableSet<Int> = HashSet()
    val ruleActionByGid: MutableMap<Int, Action> = LinkedHashMap()

    val keys: Set<Int> = moves.keys

    fun getConflicts(): ConflictList {
        return conflicts
    }

    fun getMoves(): Map<Int, Move> {
        return moves
    }

    fun addMove(mask: BitSet, localToGlobal: IntArray, move: Move) {
        if (stopAfterOne && moves.isNotEmpty()) return

        val gid = move.gid
        val existing = moves[gid]

        if (board.cells[gid].isFlagged or board.cells[gid].isRevealed) return

        if (gid in conflicts.keys) {
            conflicts.addConflict(gid, move.reasons)
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

        conflicts.addConflict(gid, existing.reasons)
        conflicts.addConflict(gid, move.reasons)


//        existing.reasons.forEach { r ->
//            if (!bucket.contains(r)) bucket.add(r)
//        }
//        move.reasons.forEach { r ->
//            if (!bucket.contains(r)) bucket.add(r)
//        }

        // add the entire scope to the conflictsGids array so they can be
        // highlighted in the front-end
        addConflicts(mask, localToGlobal, conflicts.getReasons(gid))

        moves.remove(gid)
    }

    fun addConflict(gid: Int, reasons: List<String>) {
        if (board.cells[gid].isFlagged or board.cells[gid].isRevealed) return

        conflicts.addConflict(gid, reasons)

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

    fun addConflicts(mask: BitSet, localToGlobal: IntArray, reason: List<String>) {
        var bit = mask.nextSetBit(0)
        while (bit >= 0) {
            val gid = localToGlobal[bit]
            addConflict(gid, reason)
            bit = mask.nextSetBit(bit + 1)
        }
    }

    fun isNotEmpty(): Boolean {
        return moves.isNotEmpty() and conflicts.isNotEmpty()
    }

}