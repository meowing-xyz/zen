package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.utils.ChatUtils
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object worldage {
    val mc = Minecraft.getMinecraft()

    @JvmStatic
    fun initialize() {
        Zen.registerListener("worldagechat", this)
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        MinecraftForge.EVENT_BUS.register(onTick)
    }

    object onTick {
        @SubscribeEvent
        fun onClientTick(event: TickEvent.ClientTickEvent) {
            if (event.phase != TickEvent.Phase.END || mc.theWorld == null) return

            val daysRaw = mc.theWorld.worldTime / 24000.0
            val days =
                if (daysRaw % 1.0 == 0.0) daysRaw.toInt().toString()
                else "%.1f".format(daysRaw)

            ChatUtils.addMessage("§c[Zen] §fWorld is §b$days §fdays old.")
            MinecraftForge.EVENT_BUS.unregister(this)
        }

    }
}