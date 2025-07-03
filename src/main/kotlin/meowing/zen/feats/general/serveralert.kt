package meowing.zen.feats.general

import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting

object serveralert : Feature("serveralert") {
    private val regex = "Sending to server (.+)\\.\\.\\.".toRegex()
    private val servers = mutableMapOf<String, Long>()

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            regex.find(event.event.message.unformattedText.removeFormatting())?.let { match ->
                val server = match.groupValues[1]
                val currentTime = System.currentTimeMillis()

                servers[server]?.let { lastJoined ->
                    ChatUtils.addMessage("§c[Zen] §fLast joined §b$server §f- §b${(currentTime - lastJoined) / 1000}s §fago")
                }

                servers[server] = currentTime
            }
        }
    }
}