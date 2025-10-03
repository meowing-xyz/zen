package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.Zen
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.utils.Utils
import xyz.meowing.zen.utils.Utils.removeFormatting

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
            .addElement("Meowing", "Meow Sounds", "", ConfigElement(
                "",
                null,
                ElementType.TextParagraph("Plays a meow sound when a message containing 'meow' is received in chat.")
            ))
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