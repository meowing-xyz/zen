package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.events.EventBus
import meowing.zen.events.TickEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.WorldEvent
import meowing.zen.utils.TickUtils

@Zen.Module
object WorldAge : Feature("worldagechat") {
    private var tickCall: EventBus.EventCall? = null

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "World age", ConfigElement(
                "worldagechat",
                "World age message",
                "Sends the world age in your chat.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<WorldEvent.Load> {
            tickCall?.unregister()

            tickCall = EventBus.register<TickEvent.Client>({ _ ->
                if (mc.theWorld == null) return@register

                TickUtils.schedule(20) {
                    val daysRaw = mc.theWorld.worldTime / 24000.0
                    val days = if (daysRaw % 1.0 == 0.0) daysRaw.toInt().toString() else "%.1f".format(daysRaw)

                    ChatUtils.addMessage("§c[Zen] §fWorld is §b$days §fdays old.")
                }

                tickCall?.unregister()
                tickCall = null
            })
        }
    }
}