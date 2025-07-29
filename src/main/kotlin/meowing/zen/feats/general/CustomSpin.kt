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
object CustomSpin : Feature("customspin") {
    private val customspinspeed by ConfigDelegate<Double>("customspinspeed")
    private val spineveryone by ConfigDelegate<Boolean>("spineveryone")
    private val spindirection by ConfigDelegate<Double>("spindirection")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Custom spin", ConfigElement(
                "customspin",
                "Custom spin",
                "Spins your player around!",
                ElementType.Switch(false)
            ))
            .addElement("General", "Custom spin", ConfigElement(
                "spineveryone",
                "Spin everyone",
                "Disable to only spin your player model, enable to spin all players.",
                ElementType.Switch(true),
                { config -> config["customspin"] as? Boolean == true }
            ))
            .addElement("General", "Custom spin", ConfigElement(
                "spindirection",
                "Custom spin direction",
                "The direction in which the player will rotate",
                ElementType.Dropdown(listOf("Right", "Left"), 1),
                { config -> config["customspin"] as? Boolean == true }
            ))
            .addElement("General", "Custom spin", ConfigElement(
                "customspinspeed",
                "Custom spin speed",
                "Speed for the spin rotation",
                ElementType.Slider(1.0, 20.0, 5.0, true),
                { config -> config["customspin"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (spineveryone || event.player == player) {
                GlStateManager.pushMatrix()
                GlStateManager.rotate(getRotation(), 0f, 1f, 0f)
            }
        }

        register<RenderEvent.Player.Post> { event ->
            if (spineveryone || event.player == player) GlStateManager.popMatrix()
        }
    }

    /*
     * Modified from NoammAddons code
     * Under GPL 3.0 License
     */
    private fun getRotation(): Float {
        val millis = System.currentTimeMillis() % 4000
        val fraction = millis / 4000f
        val angle = (fraction * 360f) * customspinspeed.toFloat()
        return if (spindirection == 0.0) angle - 180f else 180f - angle
    }
}