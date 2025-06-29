package meowing.zen.feats.dungeons

import meowing.zen.Zen.Companion.mc
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.events.ChatReceiveEvent
import meowing.zen.events.ServerTickEvent
import meowing.zen.events.RenderEvent
import meowing.zen.hud.HUDEditor
import meowing.zen.hud.HUDManager
import net.minecraftforge.client.event.RenderGameOverlayEvent

object firefreeze : Feature("firefreeze", area = "catacombs") {
    var ticks = 0
    private var ticking = false

    override fun initialize() {
        HUDManager.registerElement("FireFreeze", "§bFire freeze: §c4.3s")

        register<ChatReceiveEvent> { event ->
            if (event.event.type.toInt() == 2) return@register
            if (event.event.message.unformattedText.removeFormatting() == "[BOSS] The Professor: Oh? You found my Guardians' one weakness?") {
                ticks = 100
                ticking = true
                TickUtils.scheduleServer(105) {
                    Utils.playSound("random.anvil_land", 1f, 0.5f)
                    ticks = 0
                    ticking = false
                }
            }
        }

        register<ServerTickEvent> { event ->
            if (ticking && ticks > 0) {
                ticks--
                if (ticks <= 0) ticking = false
            }
        }

        register<RenderEvent> { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.TEXT) FireFreezeTimer.render()
        }
    }

    override fun onRegister() {
        ticks = 0
        ticking = false
    }

    override fun onUnregister() {
        ticks = 0
        ticking = false
    }
}

object FireFreezeTimer {
    private const val name = "FireFreeze"

    fun render() {
        val x = HUDEditor.getX(name)
        val y = HUDEditor.getY(name)
        val text = getText()

        if (text.isNotEmpty()) mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFFFFF)
    }

    private fun getText(): String {
        if (firefreeze.ticks > 0) return "§bFire freeze: §c${"%.1f".format(firefreeze.ticks / 20.0)}s"
        return ""
    }
}