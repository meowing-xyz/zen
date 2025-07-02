package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting

object leapannounce : Feature("leapannounce") {
    val regex = "^You have teleported to (.+)".toRegex()
    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val result = regex.find(event.event.message.unformattedText.removeFormatting())
            if (result != null) ChatUtils.command("/pc ${Zen.config.leapmessage} ${result.groupValues[1]}")
        }
    }
}