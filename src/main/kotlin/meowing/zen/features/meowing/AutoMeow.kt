package meowing.zen.features.meowing

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.features.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import kotlin.random.Random

@Zen.Module
object AutoMeow : Feature("automeow") {
    private val meows = arrayOf("mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew", "mrow :3", "purrr :3")
    private val channels = mapOf(
        "Guild >" to ("gc" to 0),
        "Party >" to ("pc" to 1),
        "Officer >" to ("oc" to 2),
        "Co-op >" to ("cc" to 3),
        "From " to ("r" to 4)
    )
    private val automeowchannels by ConfigDelegate<Set<Int>>("automeowchannels")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Auto meow", ConfigElement(
                "automeow",
                "Auto Meow",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Meowing", "Auto meow", "", ConfigElement(
                "",
                null,
                ElementType.TextParagraph("Replies to messages saying \"meow\" with a random meow. Works in Guild, Party, Officer, Co-op, and Private Messages.")
            ))
            .addElement("Meowing", "Auto meow", "Options", ConfigElement(
                "automeowchannels",
                "Auto Meow Response Channels",
                ElementType.MultiCheckbox(
                    options = listOf("Guild", "Party", "Officer", "Co-op", "Private Messages"),
                    default = setOf(0, 1, 2, 3, 4)
                )
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.event.message.unformattedText.removeFormatting()

            if (text.contains(player?.name!!) || !text.endsWith("meow")) return@register

            val (cmd, channelIndex) = channels.entries.firstOrNull { text.startsWith(it.key) }?.value ?: ("ac" to -1)

            if (channelIndex !in automeowchannels) return@register

            TickUtils.schedule(Random.nextLong(10, 50)) {
                ChatUtils.command("$cmd ${meows.random()}")
            }
        }
    }
}