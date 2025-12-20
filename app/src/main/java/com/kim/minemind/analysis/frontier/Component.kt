package com.kim.minemind.analysis.frontier

class Component(
    val k: Int = 0,
    val constraints: List<Constraint>,
    val localToGlobal: IntArray
) {
    fun gidToRC(gid: Int, cols: Int): Pair<Int, Int> = (gid / cols) to (gid % cols)
}