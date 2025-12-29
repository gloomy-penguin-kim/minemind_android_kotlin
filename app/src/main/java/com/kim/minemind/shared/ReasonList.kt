package com.kim.minemind.shared

class ReasonList (
) {
    private var reasons: MutableList<String> = mutableListOf()

    fun addReasons(rList: List<String>) {
        for (r in rList) {
            addReason(r)
        }
    }
    fun addReason(r: String) {
        if (r !in reasons) {
            reasons.add(r)
        }
    }
    fun getReasons(): List<String>{
        return reasons.toList()
    }
    fun addReasons(other: ReasonList) {
        for (r in other.getReasons()) {
            addReason(r)
        }
    }
    fun contains(r: String): Boolean {
        return r in reasons
    }
}