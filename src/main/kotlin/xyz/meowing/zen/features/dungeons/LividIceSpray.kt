package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.events.WorldEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.hud.HUDManager
import xyz.meowing.zen.utils.Render2D
import xyz.meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object LividIceSpray : Feature("lividicespray", area = "catacombs", subarea = listOf("F5", "M5")) {
    private var bossticks = 390

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Livid Ice Spray Timer", ConfigElement(
                "lividicespray",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        HUDManager.register("Livid ice spray timer", "§bIce spray: §c13.2s")

        createCustomEvent<RenderEvent.Text>("render") { event ->
            if (HUDManager.isEnabled("Livid ice spray timer")) render()
        }

        register<ChatEvent.Receive> { event ->
            if (event.event.message.unformattedText.removeFormatting() == "[BOSS] Livid: Welcome, you've arrived right on time. I am Livid, the Master of Shadows.") {
                createTimer(390,
                    onTick = {
                        bossticks--
                    },
                    onComplete = {
                        cleanup()
                    }
                )
                registerEvent("render")
            }
        }

        register<WorldEvent.Change> { cleanup() }
    }

    private fun cleanup() {
        bossticks = 390
        unregisterEvent("render")
    }

    private fun render() {
        val x = HUDManager.getX("Livid ice spray timer")
        val y = HUDManager.getY("Livid ice spray timer")
        val scale = HUDManager.getScale("Livid ice spray timer")
        val time = bossticks / 20
        Render2D.renderString("§bIce spray: §c${time}s", x, y, scale)
    }
}