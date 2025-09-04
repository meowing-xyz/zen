package meowing.zen.features.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object FireFreezeTimer : Feature("firefreeze", area = "catacombs", subarea = listOf("F3", "M3")) {
    private const val name = "FireFreeze"
    var ticks = 0

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Fire Freeze Timer", ConfigElement(
                "firefreeze",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        HUDManager.register("Fire Freeze", "§bFire freeze: §c4.3s")

        createCustomEvent<RenderEvent.Text>("render") { event ->
            if (HUDManager.isEnabled("Fire Freeze")) render()
        }

        register<ChatEvent.Receive> { event ->
            if (event.event.message.unformattedText.removeFormatting() == "[BOSS] The Professor: Oh? You found my Guardians' one weakness?") {
                createTimer(105,
                    onTick = {
                        if (ticks > 0) ticks--
                    },
                    onComplete = {
                        Utils.playSound("random.anvil_land", 1f, 0.5f)
                        ticks = 0
                        unregisterEvent("render")
                    }
                )
                ticks = 100
                registerEvent("render")
            }
        }
    }

    override fun onRegister() {
        ticks = 0
        super.onRegister()
    }

    override fun onUnregister() {
        ticks = 0
        super.onUnregister()
    }

    private fun render() {
        if (ticks <= 0) return

        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)
        val text = "§bFire freeze: §c${"%.1f".format(ticks / 20.0)}s"

        Render2D.renderString(text, x, y, scale)
    }
}