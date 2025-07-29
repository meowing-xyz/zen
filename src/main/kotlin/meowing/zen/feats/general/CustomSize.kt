package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import net.minecraft.client.renderer.GlStateManager

@Zen.Module
object CustomSize : Feature("customsize") {
    private val customX by ConfigDelegate<Double>("customX")
    private val customY by ConfigDelegate<Double>("customY")
    private val customZ by ConfigDelegate<Double>("customZ")
    private val scaleeveryone by ConfigDelegate<Boolean>("scaleeveryone")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Custom size", ConfigElement(
                "customsize",
                "Custom player model size",
                "Changes the size of your player model",
                ElementType.Switch(false)
            ))
            .addElement("General", "Custom size", ConfigElement(
                "customX",
                "Custom X",
                "X scale",
                ElementType.Slider(0.1, 5.0, 1.0, true),
                { config -> config["customsize"] as? Boolean == true }
            ))
            .addElement("General", "Custom size", ConfigElement(
                "customY",
                "Custom Y",
                "Y scale",
                ElementType.Slider(0.1, 5.0, 1.0, true),
                { config -> config["customsize"] as? Boolean == true }
            ))
            .addElement("General", "Custom size", ConfigElement(
                "customZ",
                "Custom Z",
                "Z scale",
                ElementType.Slider(0.1, 5.0, 1.0, true),
                { config -> config["customsize"] as? Boolean == true }
            ))
            .addElement("General", "Custom size", ConfigElement(
                "scaleeveryone",
                "Scale everyone",
                "Disable to only scale your player model, enable to scale all players.",
                ElementType.Switch(true),
                { config -> config["customsize"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (scaleeveryone || event.player == player) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(customX, customY, customZ)
            }
        }

        register<RenderEvent.Player.Post> { event ->
            if (scaleeveryone || event.player == player) GlStateManager.popMatrix()
        }
    }
}