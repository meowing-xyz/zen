package meowing.zen.feats.meowing

import meowing.zen.Zen
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object meowsounds {
    private val meowregex = Pattern.compile("(?:Guild|Party|Co-op|From|To)? ?(?:>)? ?(?:\\[.+?])? ?(?:[a-zA-Z0-9_]+) ?(?:\\[.+?])?: (.+)")
    @JvmStatic
    fun initialize() {
        Zen.registerListener("meowsounds", this)
    }

    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        val content = event.message.unformattedText.lowercase()
        val matcher = meowregex.matcher(content)
        if (!matcher.matches() || !matcher.group(1).contains("meow")) return
        val mc = Minecraft.getMinecraft()
        mc.theWorld?.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "mob.cat.meow", 0.8f, 1.0f, false)
    }
}