package meowing.zen.features.dungeons

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.event.ClickEvent

@Zen.Module
object ArchitectDraft : Feature("architectdraft", area = "catacombs") {
    private val puzzlefail = "^PUZZLE FAIL! (\\w{1,16}) .+$".toRegex()
    private val quizfail = "^\\[STATUE] Oruo the Omniscient: (\\w{1,16}) chose the wrong answer! I shall never forget this moment of misrememberance\\.$".toRegex()
    private val autogetdraft by ConfigDelegate<Boolean>("autogetdraft")
    private val selfdraft by ConfigDelegate<Boolean>("selfdraft")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Architect Draft Message", "Options", ConfigElement(
                "architectdraft",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Dungeons", "Architect Draft Message", "Options", ConfigElement(
                "selfdraft",
                "Only get drafts on your fails",
                ElementType.Switch(false)
            ))
            .addElement("Dungeons", "Architect Draft Message", "Options", ConfigElement(
                "autogetdraft",
                "Automatically get architect drafts",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.event.message.unformattedText.removeFormatting()
            val matchResult = puzzlefail.find(text) ?: quizfail.find(text) ?: return@register

            if (matchResult.groupValues[1] != player?.name && selfdraft) return@register

            if (autogetdraft) {
                TickUtils.schedule(40) {
                    ChatUtils.command("/gfs architect's first draft 1")
                }
            } else {
                ChatUtils.addMessage(
                    "$prefix §bClick to get Architect's First Draft from Sack.",
                    clickAction = ClickEvent.Action.RUN_COMMAND,
                    clickValue = "/gfs architect's first draft 1"
                )
            }
        }
    }
}