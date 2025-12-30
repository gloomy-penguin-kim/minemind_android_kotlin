package com.kim.minemind.shared

class ConflictList {

    private val conflicts: MutableMap<Int, ReasonList> = LinkedHashMap()

    val empty: Boolean get() = conflicts.isEmpty()
    val size: Int get() = conflicts.size
    val keys: Set<Int> get() = conflicts.keys


    override fun toString(): String {
        return "ConflictList(keys=${keys.toList()})"
    }

    fun addConflict(gid: Int, reason: String) {
        if (conflicts.containsKey(gid)) {
            conflicts[gid]?.addReason(reason)
        }
        else {
            conflicts[gid] = ReasonList(reason)
        }
    }

    fun addConflict(gid: Int, reasonList: ReasonList) {
        if (!conflicts.containsKey(gid)) {
            conflicts[gid] = ReasonList()
        }
        conflicts[gid]?.addReasons(reasonList)
    }

    fun addConflict(gid: Int, reasonList: List<String>) {
        if (!conflicts.containsKey(gid)) {
            conflicts[gid] = ReasonList()
        }
        conflicts[gid]?.addReasons(reasonList)
    }

    fun getConflicts(): Map<Int, ReasonList> {
        return conflicts.toMap()
    }

    fun merge(other: ConflictList): ConflictList {
        val merged = ConflictList()
        merged.conflicts.putAll(getConflicts())
        for ((gid, reasons) in other.getConflicts()) {
            if (merged.conflicts.containsKey(gid)) {
                merged.conflicts[gid]?.addReasons(reasons)
            }
            else {
                merged.conflicts[gid] = reasons
            }
        }
        return merged
    }

    fun getReasons(gid: Int): List<String> {
        return conflicts[gid]?.getReasons() ?: emptyList()
    }

    fun get(gid: Int) : ReasonList? {
        return conflicts[gid]
    }

    fun isNotEmpty(): Boolean {
        return conflicts.isNotEmpty()
    }

    fun remove(gid: Int) {
        conflicts.remove(gid)
    }

    fun applyDelta(delta: ConflictDelta): ConflictList {
        val out = ConflictList()
        // copy current
        for ((gid, reasons) in this.getConflicts()) {
            out.addConflict(gid, reasons)
        }
        // upsert new/changed
        for ((gid, reasons) in delta.upserts.getConflicts()) {
            out.addConflict(gid, reasons)
        }
        // remove resolved
        for (gid in delta.removes) out.remove(gid)
        return out
    }

}