package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.events.TickEvent
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import net.minecraftforge.client.event.RenderGameOverlayEvent

@Zen.Module
object VanilaHPHUD : Feature("vanillahphud") {
    private var hp = 0f

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Vanilla HP Hud", ConfigElement(
                "vanillahphud",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        HUDManager.register("HP Hud", "§b10 §c♥")

        register<TickEvent.Client> {
            hp = player?.health?.div(2f) ?: 0f
        }

        register<RenderEvent.HUD> { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.TEXT && HUDManager.isEnabled("HP Hud")) render()
        }
    }

    fun render() {
        val x = HUDManager.getX("HP Hud")
        val y = HUDManager.getY("HP Hud")
        val scale = HUDManager.getScale("HP Hud")
        if (hp == 0f) return
        val text = if (hp % 1f == 0f) hp.toInt() else "%.1f".format(hp)
        Render2D.renderStringWithShadow("§b$text §c❤", x, y, scale)
    }
}