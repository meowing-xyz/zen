package meowing.zen.feats.meowing

import meowing.zen.Zen
import meowing.zen.feats.Feature
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern
import javax.rmi.CORBA.Util

@Zen.Module
object MeowSounds : Feature("meowsounds") {
    private val meowRegex = Regex("(?:Guild|Party|Co-op|From|To)? ?>? ?(?:\\[.+?])? ?[a-zA-Z0-9_]+ ?(?:\\[.+?])?: (.+)")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Meow Sounds", ConfigElement(
                "meowsounds",
                "Meow Sounds",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val content = event.event.message.unformattedText.removeFormatting()
            val match = meowRegex.find(content) ?: return@register
            if (match.groups[1]?.value?.contains("meow", ignoreCase = true) != true) return@register
            Utils.playSound("mob.cat.meow", 0.8f, 1.0f)
        }
    }
}