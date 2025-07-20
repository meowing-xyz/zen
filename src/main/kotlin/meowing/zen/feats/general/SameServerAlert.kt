package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object SameServerAlert : Feature("serveralert") {
    private val regex = "Sending to server (.+)\\.\\.\\.".toRegex()
    private val servers = mutableMapOf<String, Long>()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Server alert", ConfigElement(
                "serveralert",
                "Same server alert",
                "Shows a chat message when you join a server you previously joined.",
                ElementType.Switch(false)
            ))
    }

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