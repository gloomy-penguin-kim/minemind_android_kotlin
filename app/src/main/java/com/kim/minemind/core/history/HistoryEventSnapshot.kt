package com.kim.minemind.core.history
import kotlinx.serialization.Serializable
import com.kim.minemind.core.Action

@Serializable
data class HistoryEventSnapshot(
    val type: Type,
    val action: String? = null,
    val gid: Int? = null,
    val flagValue: Boolean? = null,
    val count: Int? = null,
    val note: String? = null
) {
    fun toEvent(): HistoryEvent {
        return when (type) {
            Type.USER_COMMAND -> {
                HistoryEvent.UserCommand(
                    action = Action.valueOf(action!!),
                    gid = gid,
                    flagValue = flagValue
                )
            }
            Type.AUTO_STEP -> {
                HistoryEvent.Auto(note = note)
            }
            Type.APPLY_RECOMMENDATIONS -> {
                HistoryEvent.ApplyRecommendations(count = count)
            }
            Type.SYSTEM -> {
                HistoryEvent.System(note = note)
            }
        }
    }

    @Serializable
    enum class Type {
        USER_COMMAND,
        AUTO_STEP,
        APPLY_RECOMMENDATIONS,
        SYSTEM
    }
}
