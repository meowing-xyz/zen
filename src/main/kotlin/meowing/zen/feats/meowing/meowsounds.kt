package meowing.zen.feats.meowing

import meowing.zen.Zen
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class meowsounds private constructor() {
    companion object {
        private val instance = meowsounds()

        @JvmStatic
        fun initialize() {
            Zen.registerListener("meowsounds", instance)
        }
    }

    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        val content = event.message.unformattedText.lowercase()
        if (content.contains("meow")) {
            val mc = Minecraft.getMinecraft()
            mc.theWorld?.playSound(
                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ,
                "mob.cat.meow", 0.8f, 1.0f, false
            )
        }
    }
}