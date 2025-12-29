package com.kim.minemind.core.board

import com.kim.minemind.core.history.ChangeSet

data class AppliedMove (
    val changeSet: ChangeSet = ChangeSet() ,
    val conflicts:  MutableMap<Int, MutableSet<String>> = LinkedHashMap()
)

