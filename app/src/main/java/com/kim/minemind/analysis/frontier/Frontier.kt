package com.kim.minemind.analysis.frontier

import DisjointSetUnion
import com.kim.minemind.core.board.Board
import java.util.BitSet


class Frontier (
) {
    fun constraintsAndUnknownNeighbors(board: Board): Pair<List<Scope>, IntArray> {
        val scopes = ArrayList<Scope>()
        val seen = HashSet<String>()
        val allUnknowns = HashSet<Int>()

        for (cell in board.cells) {
            if (!cell.isRevealed) continue
            if (cell.adjacentMines <= 0) continue
            if (cell.isFlagged) continue
            if (cell.isMine) continue

            var flagged = 0
            val unknownNeighbors = ArrayList<Int>()

            for (gid in board.neighbors(cell.gid)) {
                if (board.cells[gid].isFlagged) flagged++
                else if (!board.cells[gid].isRevealed) unknownNeighbors.add(gid)
            }

            if (unknownNeighbors.isEmpty()) continue

            val remaining = cell.adjacentMines - flagged

            val gids = IntArray(unknownNeighbors.size)

            for (i in unknownNeighbors.indices) {
                val gid = unknownNeighbors[i]
                gids[i] = gid
                allUnknowns.add(gid)
            }
            gids.sort()

            val key = gids.joinToString(prefix = "[", postfix = "]") + "|$remaining"
            if (seen.add(key)) {
                scopes.add(Scope(gids, remaining))
            }
        }

        val allUnknownsSorted = allUnknowns.toIntArray().also { it.sort() }
        return scopes to allUnknownsSorted
    }

    fun dsuFind(globalMasks: List<BitSet>): Map<Int, List<Int>> {
        val c = globalMasks.size
        val dsu = DisjointSetUnion (c)

        for (i in 0 until c) {
            val mi = globalMasks[i]
            for (j in i + 1 until c) {
                if (mi.intersects(globalMasks[j])) dsu.union(i, j)
            }
        }

        val components = HashMap<Int, MutableList<Int>>()
        for (cid in 0 until c) {
            val root = dsu.find(cid)
            components.getOrPut(root) { ArrayList() }.add(cid)
        }
        return components
    }

    fun buildFrontier(board: Board): List<Component> {
        val (scopes, _) = constraintsAndUnknownNeighbors(board)
        if (scopes.isEmpty()) return emptyList()

        // Global mask per scope (BitSet)
        val globalMasks = ArrayList<BitSet>(scopes.size)
        for (s in scopes) {
            val bs = BitSet()
            for (gid in s.gids) bs.set(gid)
            globalMasks.add(bs)
        }

        val compsByRoot = dsuFind(globalMasks)

        val comps: MutableList<Component> = ArrayList()

        for ((_, scopeIdxs) in compsByRoot) {

            // collect distinct global gids in this DSU component
            val localToGlobalSet = HashSet<Int>()
            for (idx in scopeIdxs) for (gid in scopes[idx].gids) localToGlobalSet.add(gid)
            val localToGlobal = localToGlobalSet.toIntArray().also { it.sort() }

            // map global gid -> local index
            val globalToLocal = HashMap<Int, Int>(localToGlobal.size * 2)
            for (i in localToGlobal.indices) globalToLocal[localToGlobal[i]] = i

            // build constraints (local BitSet + global BitSet + remaining)
            val constraints = ArrayList<Constraint>(scopeIdxs.size)
            for (idx in scopeIdxs) {
                val s = scopes[idx]

                val localMask = BitSet(localToGlobal.size)
                for (gid in s.gids) {
                    val local = globalToLocal[gid]!!
                    localMask.set(local)
                }

                val globalMask = globalMasks[idx].clone() as BitSet
                constraints.add(Constraint(localMask, s.remaining))
            }

            // stable ordering: sort by local mask "signature"
            constraints.sortWith(compareBy { c: Constraint -> c.mask.cardinality() }.thenBy { it.mask.length() })

            comps.add(
                Component(
                    k = localToGlobal.size,
                    constraints = constraints,
                    localToGlobal = localToGlobal
                )
            )
        }

        comps.sortBy { c: Component -> c.localToGlobal.minOrNull() ?: Int.MAX_VALUE }
        return comps
    }

}