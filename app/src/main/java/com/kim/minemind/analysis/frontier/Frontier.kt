package com.kim.minemind.analysis.frontier

import DisjointSetUnion
import com.kim.minemind.core.board.Board
import java.util.BitSet


class Frontier (
) {
//    init {
//        val components = buildFrontier(board)
//    }

    fun constraintsAndUnknownNeighbors(board: Board): Pair<List<Scope>, IntArray> {
        val rows = board.rows
        val cols = board.cols

        val scopes = ArrayList<Scope>()
        // unique key: gids + remaining. (You can replace this with a better key type later.)
        val seen = HashSet<String>()
        val allUnknowns = HashSet<Int>()

        for (r in 0 until rows) {
            for (c in 0 until cols) {

                if (!board.isRevealed(r, c)) continue

                val adj = board.adjMines(r, c)
                if (adj <= 0) continue

                var flagged = 0
                val unknownNeighbors = ArrayList<Pair<Int, Int>>()

                for ((nr, nc) in board.neighbors(r, c)) {
                    if (board.isFlagged(nr, nc)) flagged++
                    else if (!board.isRevealed(nr, nc)) unknownNeighbors.add(nr to nc)
                }

                if (unknownNeighbors.isEmpty()) continue

                val remaining = adj - flagged

                val gids = IntArray(unknownNeighbors.size)
                for (i in unknownNeighbors.indices) {
                    val (nr, nc) = unknownNeighbors[i]
                    val gid = nr * cols + nc
                    gids[i] = gid
                    allUnknowns.add(gid)
                }
                gids.sort()

                val key = gids.joinToString(prefix = "[", postfix = "]") + "|$remaining"
                if (seen.add(key)) {
                    scopes.add(Scope(gids, remaining))
                }
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