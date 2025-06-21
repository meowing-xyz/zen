package meowing.zen.feats.meowing

import meowing.zen.events.ChatReceiveEvent
import meowing.zen.feats.Feature
import meowing.zen.Zen.Companion.mc
import java.util.regex.Pattern

object meowsounds : Feature("meowsounds") {
    private val meowregex = Pattern.compile("(?:Guild|Party|Co-op|From|To)? ?(?:>)? ?(?:\\[.+?])? ?(?:[a-zA-Z0-9_]+) ?(?:\\[.+?])?: (.+)")

    override fun initialize() {
        register<ChatReceiveEvent> {
            val content = it.event.message.unformattedText.lowercase()
            val matcher = meowregex.matcher(content)
            if (!matcher.matches() || !matcher.group(1).contains("meow")) return@register
            mc.theWorld?.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "mob.cat.meow", 0.8f, 1.0f, false)
        }
    }
}