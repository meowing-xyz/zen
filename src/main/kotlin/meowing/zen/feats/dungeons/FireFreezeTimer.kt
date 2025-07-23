package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.events.EventBus
import meowing.zen.events.RenderEvent
import meowing.zen.events.TickEvent
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import net.minecraftforge.client.event.RenderGameOverlayEvent

@Zen.Module
object FireFreezeTimer : Feature("firefreeze", area = "catacombs", subarea = listOf("F3", "M3")) {
    private const val name = "FireFreeze"
    var ticks = 0
    private var servertickcall = EventBus.register<TickEvent.Server> ({
        if (ticks > 0) ticks--
    }, false)

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Fire freeze", ConfigElement(
                "firefreeze",
                "Fire freeze timer",
                "Time until you should activate fire freeze",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.register("FireFreeze", "§bFire freeze: §c4.3s")

        register<ChatEvent.Receive> { event ->
            if (event.event.type.toInt() == 2) return@register
            if (event.event.message.unformattedText.removeFormatting() == "[BOSS] The Professor: Oh? You found my Guardians' one weakness?") {
                ticks = 100
                servertickcall.register()
                TickUtils.scheduleServer(105) {
                    Utils.playSound("random.anvil_land", 1f, 0.5f)
                    ticks = 0
                    servertickcall.unregister()
                }
            }
        }

        register<RenderEvent.HUD> { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.TEXT) render()
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
        if (!HUDManager.isEnabled("firefreeze") || ticks <= 0) return

        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)
        val text = "§bFire freeze: §c${"%.1f".format(ticks / 20.0)}s"

        if (text.isNotEmpty()) Render2D.renderStringWithShadow(text, x, y, scale)
    }
}