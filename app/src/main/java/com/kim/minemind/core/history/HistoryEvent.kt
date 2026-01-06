package com.kim.minemind.core.history

import com.kim.minemind.core.Action
import com.kim.minemind.shared.HistoryEntrySnapshot

sealed class HistoryEvent {
    data class UserCommand(val action: Action, val gid: Int, val flagValue: Boolean? = null) : HistoryEvent()
    data class Auto(val note: String = "") : HistoryEvent()              // “Auto step”
    data class ApplyRecommendations(val count: Int) : HistoryEvent()         // if you do batch apply
    data class System(val note: String) : HistoryEvent()                     // fallback
}


// shared module
sealed class HistoryEventSnapshot {
    data class UserCommand(val actionName: String, val gid: Int, val flagValue: Boolean?) : HistoryEventSnapshot()
    data class Auto(val note: String) : HistoryEventSnapshot()
    data class ApplyRecommendations(val count: Int) : HistoryEventSnapshot()
    data class System(val note: String) : HistoryEventSnapshot()
}


fun HistoryEvent.toSnapshot(): HistoryEventSnapshot = when (this) {
    is HistoryEvent.UserCommand ->
        HistoryEventSnapshot.UserCommand(
            actionName = action.name, // or action.id if you have one
            gid = gid,
            flagValue = flagValue
        )

    is HistoryEvent.Auto ->
        HistoryEventSnapshot.Auto(note)

    is HistoryEvent.ApplyRecommendations ->
        HistoryEventSnapshot.ApplyRecommendations(count)

    is HistoryEvent.System ->
        HistoryEventSnapshot.System(note)
}

fun HistoryEventSnapshot.toEvent(): HistoryEvent = when (this) {
    is HistoryEventSnapshot.UserCommand ->
        HistoryEvent.UserCommand(
            action = Action.valueOf(actionName),
            gid = gid,
            flagValue = flagValue
        )

    is HistoryEventSnapshot.Auto ->
        HistoryEvent.Auto(note)

    is HistoryEventSnapshot.ApplyRecommendations ->
        HistoryEvent.ApplyRecommendations(count)

    is HistoryEventSnapshot.System ->
        HistoryEvent.System(note)
}
