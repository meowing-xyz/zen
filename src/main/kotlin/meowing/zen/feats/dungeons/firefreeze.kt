package meowing.zen.feats.dungeons

import cc.polyfrost.oneconfig.hud.TextHud
import meowing.zen.Zen
import meowing.zen.events.ChatReceiveEvent
import meowing.zen.events.ServerTickEvent
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object firefreeze {
    var ticks = 0

    @JvmStatic
    fun initialize() {
        Zen.registerListener("firefreeze", this)
    }

    @SubscribeEvent
    fun onMessageReceive(event: ChatReceiveEvent) {
        if (event.packet.type.toInt() == 2) return
        if (event.packet.chatComponent.unformattedText.removeFormatting() == "[BOSS] The Professor: Oh? You found my Guardians' one weakness?") {
            ticks = 100
            MinecraftForge.EVENT_BUS.register(ticktimer)
            TickScheduler.scheduleServer(105) {
                Utils.playSound("random.anvil_land", 1f, 0.5f)
                ticks = 0
            }
        }
    }

    object ticktimer {
        @SubscribeEvent
        fun onServerTick(event: ServerTickEvent) {
            if (ticks-- <= 1) MinecraftForge.EVENT_BUS.unregister(this)
        }
    }
}

class FireFreezeHud : TextHud(true, 200, 100) {
    override fun getLines(lines: MutableList<String>, example: Boolean) {
        if (example) {
            lines.add("§bFire freeze: §c4.3s")
            return
        }

        if (firefreeze.ticks > 0) lines.add("§bFire freeze: §c${"%.1f".format(firefreeze.ticks / 20.0)}")
    }
}