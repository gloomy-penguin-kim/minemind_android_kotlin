//package com.kim.minemind.analysis.rules
//
//
//class MoveList(private val one: Boolean = false) {
//
//    // gid -> move
//    private val moves: MutableMap<Int, Move> = LinkedHashMap()
//
//    // gid -> set of reasons that conflicted at this gid
//    private val conflicts: MutableMap<Int, MutableSet<String>> = LinkedHashMap()
//
//    fun addMove(move: Move) {
//        if (one && moves.isNotEmpty()) return
//
//        val gid = move.gid
//        val existing = moves[gid]
//
//        if (existing == null) {
//            moves[gid] = move
//            return
//        }
//
//        // If same action, keep the first (matching Python behavior)
//        if (existing.action == move.action) return
//
//        // Conflict: record both reasons and remove the move from moves
//        val bucket = conflicts.getOrPut(gid) { LinkedHashSet() }
//
//        existing.reasons.firstOrNull()?.let { bucket.add(it) }
//        move.reasons.firstOrNull()?.let { bucket.add(it) }
//
//        moves.remove(gid)
//    }
//
////    /**
////     * Like Python get_arrays(): returns (movesList, conflictsEntries)
////     * - movesList: List<Move>
////     * - conflictsEntries: List<Pair<gid, List<reasons>>>
////     */
////    fun getArrays(): Pair<List<Move>, List<Pair<Int, List<String>>>> {
////        val movesList = moves.values.toList()
////        val conflictsList = conflicts.entries.map { (gid, reasonsSet) ->
////            gid to reasonsSet.toList()
////        }
////        return movesList to conflictsList
////    }
//}
