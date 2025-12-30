package com.kim.minemind.core.board

import com.kim.minemind.core.history.ChangeSet
import com.kim.minemind.shared.ConflictDelta

data class AppliedMove(
    val changeSet: ChangeSet = ChangeSet(),
    val conflictDelta: ConflictDelta = ConflictDelta()
) {
    val empty: Boolean get() = changeSet.empty && conflictDelta.upserts.empty && conflictDelta.removes.isEmpty()
}
