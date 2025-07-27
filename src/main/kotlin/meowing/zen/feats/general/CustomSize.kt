package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import kotlin.math.abs

@Zen.Module
object CustomSize : Feature("customsize") {
    private val customX by ConfigDelegate<Double>("customX")
    private val customY by ConfigDelegate<Double>("customY")
    private val customZ by ConfigDelegate<Double>("customZ")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Custom model", ConfigElement(
                "customsize",
                "Custom player model size",
                "Changes the size of your player model",
                ElementType.Switch(false)
            ))
            .addElement("General", "Custom model", ConfigElement(
                "customX",
                "Custom X",
                "X scale",
                ElementType.Slider(0.1, 5.0, 1.0, true),
                { config -> config["customsize"] as? Boolean == true }
            ))
            .addElement("General", "Custom model", ConfigElement(
                "customY",
                "Custom Y",
                "Y scale",
                ElementType.Slider(0.1, 5.0, 1.0, true),
                { config -> config["customsize"] as? Boolean == true }
            ))
            .addElement("General", "Custom model", ConfigElement(
                "customZ",
                "Custom Z",
                "Z scale",
                ElementType.Slider(0.1, 5.0, 1.0, true),
                { config -> config["customsize"] as? Boolean == true }
            ))
            .addElement("General", "Custom model", ConfigElement(
                "customself",
                "Only scale yourself",
                "Enable to only scale your player model, disable to scale all players.",
                ElementType.Switch(true)
            ))
    }

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (event.player is EntityPlayerSP) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(customX, abs(customY), customZ)
            }
        }

        register<RenderEvent.Player.Post> { event ->
            if (event.player is EntityPlayerSP) GlStateManager.popMatrix()
        }
    }
}