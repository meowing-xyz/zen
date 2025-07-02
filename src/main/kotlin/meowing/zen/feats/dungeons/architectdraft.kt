package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.event.ClickEvent

object architectdraft : Feature("architectdraft") {
    private val puzzlefail = "^PUZZLE FAIL! (\\w{1,16}) .+$".toRegex()
    private val quizfail = "^\\[STATUE] Oruo the Omniscient: (\\w{1,16}) chose the wrong answer! I shall never forget this moment of misrememberance\\.$".toRegex()

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.event.message.unformattedText.removeFormatting()

            puzzlefail.find(text)?.let { matchResult ->
                if (matchResult.groupValues[1] != Zen.mc.thePlayer.name && Zen.config.draftself) return@register
                if (Zen.config.autogetdraft) {
                    TickUtils.schedule(40) {
                        ChatUtils.command("/gfs architect's first draft 1")
                    }
                } else {
                    ChatUtils.addMessage(
                        "§c[Zen] §bClick to get Architect's First Draft from Sack.",
                        clickAction = ClickEvent.Action.RUN_COMMAND,
                        clickValue = "/gfs architect's first draft 1"
                    )
                }
            }

            quizfail.find(text)?.let { matchResult ->
                if (matchResult.groupValues[1] != Zen.mc.thePlayer.name && Zen.config.draftself) return@register
                if (Zen.config.autogetdraft) {
                    TickUtils.schedule(40) {
                        ChatUtils.command("/gfs architect's first draft 1")
                    }
                } else {
                    ChatUtils.addMessage(
                        "§c[Zen] §bClick to get Architect's First Draft from Sack.",
                        clickAction = ClickEvent.Action.RUN_COMMAND,
                        clickValue = "/gfs architect's first draft 1"
                    )
                }
            }
        }
    }
}