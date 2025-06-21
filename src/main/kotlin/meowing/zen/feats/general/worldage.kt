package meowing.zen.feats.general

import meowing.zen.events.EventBus
import meowing.zen.events.TickEvent
import meowing.zen.events.WorldLoadEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import net.minecraft.client.Minecraft

object worldage : Feature("worldagechat") {
    private val mc = Minecraft.getMinecraft()
    private var tickCall: EventBus.EventCall? = null

    override fun initialize() {
        register<WorldLoadEvent> {
            tickCall?.unregister()

            tickCall = EventBus.register<TickEvent>({ _ ->
                if (mc.theWorld == null) return@register

                val daysRaw = mc.theWorld.worldTime / 24000.0
                val days = if (daysRaw % 1.0 == 0.0) daysRaw.toInt().toString() else "%.1f".format(daysRaw)

                ChatUtils.addMessage("§c[Zen] §fWorld is §b$days §fdays old.")
                tickCall?.unregister()
                tickCall = null
            }, true)
        }
    }
}