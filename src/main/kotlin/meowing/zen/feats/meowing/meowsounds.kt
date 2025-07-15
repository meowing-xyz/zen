package meowing.zen.feats.meowing

import meowing.zen.Zen
import meowing.zen.feats.Feature
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import java.util.regex.Pattern

@Zen.Module
object meowsounds : Feature("meowsounds") {
    private val meowregex = Pattern.compile("(?:Guild|Party|Co-op|From|To)? ?(?:>)? ?(?:\\[.+?])? ?(?:[a-zA-Z0-9_]+) ?(?:\\[.+?])?: (.+)")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Meow Sounds", ConfigElement(
                "meowdeathsounds",
                "Meow Death Sounds",
                "Plays a cat sound whenever an entity dies",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> {
            val content = it.event.message.unformattedText.lowercase()
            val matcher = meowregex.matcher(content)
            if (!matcher.matches() || !matcher.group(1).contains("meow")) return@register
            mc.theWorld?.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "mob.cat.meow", 0.8f, 1.0f, false)
        }
    }
}