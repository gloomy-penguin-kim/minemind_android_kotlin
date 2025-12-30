package com.kim.minemind.shared


data class ConflictDelta(
    val upserts: ConflictList = ConflictList(), // conflicts to add/replace
    var removes: Set<Int> = emptySet()          // conflict gids to clear
) {
    val empty: Boolean get() = upserts.empty && removes.isEmpty()
    fun clearCell(gid: Int) {
        upserts.remove(gid)
        removes.plus(gid)
    }
}
