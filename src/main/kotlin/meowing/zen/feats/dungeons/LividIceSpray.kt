package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import meowing.zen.utils.Utils.removeFormatting
import net.minecraftforge.client.event.RenderGameOverlayEvent

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

        createCustomEvent<RenderEvent.HUD>("render") { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.TEXT && HUDManager.isEnabled("Livid ice spray timer")) render()
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
        Render2D.renderStringWithShadow("§bIce spray: §c${time}s", x, y, scale)
    }
}