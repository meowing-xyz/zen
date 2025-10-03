package xyz.meowing.zen.features.general

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.TickEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.ItemUtils.skyblockID
import xyz.meowing.zen.utils.Render2D

@Zen.Module
object ReaperTimer : Feature("reapertimer", true) {
    private const val name = "Reaper Timer"
    private var reaped = false
    private var ticks = 120

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Reaper Timer", ConfigElement(
                "reapertimer",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        HUDManager.register(name, "§c4.2s")

        createCustomEvent<RenderEvent.Text>("render") { event ->
            if (!HUDManager.isEnabled(name)) return@createCustomEvent
            val x = HUDManager.getX(name)
            val y = HUDManager.getY(name)
            val scale = HUDManager.getScale(name)
            val time = ticks / 20.0
            Render2D.renderString("§c${"%.1f".format(time)}s", x, y, scale)
        }

        register<TickEvent.Server> {
            if (player == null || reaped || !player!!.isSneaking) return@register
            if (player!!.inventory.armorInventory.drop(1).all { it.skyblockID.contains("REAPER", true) }) {
                reaped = true

                createTimer(120,
                    onTick = {
                        if (ticks > 0) ticks--
                    },
                    onComplete = {
                        ticks = 120
                        unregisterEvent("render")
                    }
                )

                createTimer(490,
                    onComplete = {
                        reaped = false
                    }
                )

                registerEvent("render")
            }
        }
    }
}