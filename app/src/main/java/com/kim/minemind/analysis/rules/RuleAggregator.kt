package com.kim.minemind.analysis.rules

import com.kim.minemind.core.Action
import com.kim.minemind.core.board.Board
import com.kim.minemind.shared.ConflictList
import java.util.BitSet
import kotlin.collections.MutableMap

class RuleAggregator(
    private val board: Board = Board(rows = 0, cols = 0, mines = 0, seed = 0),
    private val stopAfterOne: Boolean = false
) {

    // gid -> move
    private val moves: MutableMap<Int, Rule> = LinkedHashMap()

    private val conflicts: ConflictList = ConflictList()

    val forcedFlags: MutableSet<Int> = HashSet()
    val forcedOpens: MutableSet<Int> = HashSet()
    val actionByGid: MutableMap<Int, Action> = LinkedHashMap()

    val keys: Set<Int> = moves.keys

    fun getConflicts(): ConflictList {
        return conflicts
    }

    fun getRules(): Map<Int, Rule> {
        return moves
    }

    fun addRule(mask: BitSet, localToGlobal: IntArray, rule: Rule) {
        if (stopAfterOne && moves.isNotEmpty()) return

        val gid = rule.gid
        val existing = moves[gid]

        if (board.cells[gid].isFlagged or board.cells[gid].isRevealed) return

        if (gid in conflicts.keys) {
            conflicts.addConflict(gid, rule.reasons)
            return
        }

        // this is a new gid and not found in the conflicts or
        // in the
        if (existing == null) {
            if (rule.type == RuleType.MINE) {
                moves[gid] = rule
                forcedFlags.add(gid)
            }
            else if (rule.type == RuleType.SAFE) {
                moves[gid] = rule
                forcedOpens.add(gid)
            }
            actionByGid[gid] = rule.toAction()

            return
        }

        if (existing.type == rule.type) {
            if (existing.reasons != rule.reasons) {
                existing.reasons.addReasons(rule.reasons)
            }
            return
        }


       // Conflict: record both reasons and remove the move from moves

        conflicts.addConflict(gid, reasonList=existing.reasons)
        conflicts.addConflict(gid, reasonList=rule.reasons)


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

        if (actionByGid.contains(gid)) {
            actionByGid.remove(gid)
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



