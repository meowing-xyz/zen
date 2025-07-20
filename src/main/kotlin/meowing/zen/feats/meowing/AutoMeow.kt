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
    private val regex = Pattern.compile("^(?:\\w+(?:-\\w+)?\\s>\\s)?(?:\\[[^]]+]\\s)?(?:\\S+\\s)?(?:\\[[^]]+]\\s)?([A-Za-z0-9_.-]+)(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?:\\s(?:[A-Za-z0-9_.-]+(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?\\s?(?:[»>]|:)\\s)?meow$", Pattern.CASE_INSENSITIVE)
    private val meows = arrayOf("mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew")
    private val channels = mapOf("Party >" to "pc", "Guild >" to "gc", "Officer >" to "oc", "Co-op >" to "cc")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Auto meow", ConfigElement(
                "automeow",
                "Auto Meow",
                "Automatically responds with a meow message whenever someone sends meow in chat.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.event.message.unformattedText.removeFormatting()
            val player = mc.thePlayer?.name ?: return@register
            if (!regex.matcher(text).matches() || text.contains("To ") || text.contains(player)) return@register
            val cmd = if (text.startsWith("From ")) {
                regex.matcher(text).takeIf { m -> m.find() }?.group(1)?.let { "msg $it" } ?: return@register
            } else channels.entries.find { e -> text.startsWith(e.key) }?.value ?: "ac"
            TickUtils.schedule(ceil(Random.nextDouble() * 40).toLong() + 10) {
                ChatUtils.command("$cmd ${meows.random()}")
            }
        }
    }
}