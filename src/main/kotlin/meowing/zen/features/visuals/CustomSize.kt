package meowing.zen.features.visuals

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import net.minecraft.client.renderer.GlStateManager

@Zen.Module
object CustomSize : Feature("customsize") {
    val customX by ConfigDelegate<Double>("customX")
    val customY by ConfigDelegate<Double>("customY")
    val customZ by ConfigDelegate<Double>("customZ")
    private val scaleeveryone by ConfigDelegate<Boolean>("scaleeveryone")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Visuals", "Custom size", ConfigElement(
                "customsize",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Visuals", "Custom size", "Size", ConfigElement(
                "customX",
                "Custom X",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addElement("Visuals", "Custom size", "Size", ConfigElement(
                "customY",
                "Custom Y",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addElement("Visuals", "Custom size", "Size", ConfigElement(
                "customZ",
                "Custom Z",
                ElementType.Slider(0.1, 5.0, 1.0, true)
            ))
            .addElement("Visuals", "Custom size", "Other Options", ConfigElement(
                "scaleeveryone",
                "Scale everyone",
                ElementType.Switch(true)
            ))
    }

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (scaleeveryone || event.player == player) {
                GlStateManager.pushMatrix()
                GlStateManager.translate(event.x, event.y, event.z)
                GlStateManager.scale(customX, customY, customZ)
                GlStateManager.translate(-event.x, -event.y, -event.z)
            }
        }

        register<RenderEvent.Player.Post> { event ->
            if (scaleeveryone || event.player.entityId == player?.entityId) GlStateManager.popMatrix()
        }
    }
}