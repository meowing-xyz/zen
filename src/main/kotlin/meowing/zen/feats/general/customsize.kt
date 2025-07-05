package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Utils.convertToFloat
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import kotlin.math.abs

object customsize : Feature("customsize") {
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
        var x = 1.0f
        var y = 1.0f
        var z = 1.0f

        Zen.registerCallback("customX") { newVal ->
            x = convertToFloat(newVal)
        }
        Zen.registerCallback("customY") { newVal ->
            y = convertToFloat(newVal)
        }
        Zen.registerCallback("customZ") { newVal ->
            z = convertToFloat(newVal)
        }

        register<RenderEvent.Player.Pre> { event ->
            if (event.player is EntityPlayerSP) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(x, abs(y), z)
            }
        }

        register<RenderEvent.Player.Post> { event ->
            if (event.player is EntityPlayerSP) GlStateManager.popMatrix()
        }
    }
}