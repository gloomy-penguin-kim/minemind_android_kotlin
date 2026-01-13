package com.kim.minemind.core.history

import com.kim.minemind.core.Action
import com.kim.minemind.core.history.HistoryEventSnapshot.Type
import kotlinx.serialization.Serializable

sealed class HistoryEvent {
    data class UserCommand(val action: Action, val gid: Int?, val flagValue: Boolean? = null) : HistoryEvent()
    data class Auto(val note: String? = "") : HistoryEvent()                  // “Auto step”
    data class ApplyRecommendations(val count: Int?) : HistoryEvent()         // if you do batch apply
    data class System(val note: String?) : HistoryEvent()                     // fallback
}

//
//@Serializable
//data class HistoryEntrySnapshot(
//    val event: HistoryEventSnapshot,
//    val changes: ChangeSetSnapshot,
//    val moveCountBefore: Int,
//    val remainingSafeBefore: Int
//)

