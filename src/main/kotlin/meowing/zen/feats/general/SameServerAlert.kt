package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object SameServerAlert : Feature("serveralert") {
    private val regex = "Sending to server (.+)\\.\\.\\.".toRegex()
    private val servers = mutableMapOf<String, Long>()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Same server alert", ConfigElement(
                "serveralert",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            regex.find(event.event.message.unformattedText.removeFormatting())?.let { match ->
                val server = match.groupValues[1]
                val currentTime = TimeUtils.now.toMillis

                servers[server]?.let { lastJoined ->
                    ChatUtils.addMessage("$prefix §fLast joined §b$server §f- §b${(currentTime - lastJoined) / 1000}s §fago")
                }

                servers[server] = currentTime
            }
        }
    }
}