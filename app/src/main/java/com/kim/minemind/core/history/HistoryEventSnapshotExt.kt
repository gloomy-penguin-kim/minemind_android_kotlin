package com.kim.minemind.core.history


import com.kim.minemind.core.Action

fun HistoryEvent.toSnapshot(): HistoryEventSnapshot =
    when (this) {
        is HistoryEvent.UserCommand ->
            HistoryEventSnapshot(
                type = HistoryEventSnapshot.Type.USER_COMMAND,
                action = action.name,
                gid = gid,
                flagValue = flagValue
            )

        is HistoryEvent.Auto ->
            HistoryEventSnapshot(
                type = HistoryEventSnapshot.Type.AUTO_STEP,
                note = note
            )

        is HistoryEvent.ApplyRecommendations ->
            HistoryEventSnapshot(
                type = HistoryEventSnapshot.Type.APPLY_RECOMMENDATIONS,
                count = count
            )

        is HistoryEvent.System ->
            HistoryEventSnapshot(
                type = HistoryEventSnapshot.Type.SYSTEM,
                note = note
            )
    }

fun HistoryEventSnapshot.toEvent(): HistoryEvent =
    when (type) {
        HistoryEventSnapshot.Type.USER_COMMAND ->
            HistoryEvent.UserCommand(
                action = Action.valueOf(requireNotNull(action) { "action missing" }),
                gid = requireNotNull(gid) { "gid missing" },
                flagValue = flagValue
            )

        HistoryEventSnapshot.Type.AUTO_STEP ->
            HistoryEvent.Auto(note = note ?: "")

        HistoryEventSnapshot.Type.APPLY_RECOMMENDATIONS ->
            HistoryEvent.ApplyRecommendations(count = requireNotNull(count) { "count missing" })

        HistoryEventSnapshot.Type.SYSTEM ->
            HistoryEvent.System(note = note ?: "")
    }
