package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object LeapAnnounce : Feature("leapannounce") {
    private val regex = "^You have teleported to (.+)".toRegex()
    private val leapmessage by ConfigDelegate<String>("leapmessage")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Leap announce", ConfigElement(
                "leapannounce",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Dungeons", "Leap announce", "Options", ConfigElement(
                "leapmessage",
                "Leap announce message",
                ElementType.TextInput("Leaping to", "Leaping to")
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val result = regex.find(event.event.message.unformattedText.removeFormatting())
            if (result != null) ChatUtils.command("/pc $leapmessage ${result.groupValues[1]}")
        }
    }
}