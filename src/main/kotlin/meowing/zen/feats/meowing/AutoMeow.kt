package meowing.zen.feats.meowing

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.random.Random

@Zen.Module
object AutoMeow : Feature("automeow") {
    private val regex = "^(?:\\w+(?:-\\w+)?\\s>\\s)?(?:\\[[^]]+]\\s)?(?:\\S+\\s)?(?:\\[[^]]+]\\s)?([A-Za-z0-9_.-]+)(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?:\\s(?:[A-Za-z0-9_.-]+(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?\\s?(?:[»>]|:)\\s)?meow$".toRegex(RegexOption.IGNORE_CASE)
    private val meows = arrayOf("mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew", "mrow :3", "purrr :3")
    private val channels = mapOf(
        "Guild >" to "gc",
        "Party >" to "pc",
        "Officer >" to "oc",
        "Co-op >" to "cc"
    )

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Auto meow", ConfigElement(
                "automeow",
                "Auto Meow",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.event.message.unformattedText.removeFormatting()
            val matchResult = regex.find(text) ?: return@register
            val username = matchResult.groupValues[1]

            if (text.contains("To ") || username == player?.name) return@register

            val cmd = when {
                text.startsWith("From ") -> "msg $username"
                else -> channels.entries.find { text.startsWith(it.key) }?.value ?: "ac"
            }

            TickUtils.schedule(Random.nextLong(10, 50)) {
                ChatUtils.command("$cmd ${meows.random()}")
            }
        }
    }
    }