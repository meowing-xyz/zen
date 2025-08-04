package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
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
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Custom spin", "Options", ConfigElement(
                "spineveryone",
                "Spin everyone",
                ElementType.Switch(true)
            ))
            .addElement("General", "Custom spin", "Options", ConfigElement(
                "spindirection",
                "Custom spin direction",
                ElementType.Dropdown(listOf("Right", "Left"), 1)
            ))
            .addElement("General", "Custom spin", "Options", ConfigElement(
                "customspinspeed",
                "Custom spin speed",
                ElementType.Slider(1.0, 20.0, 5.0, true)
            ))
    }

    override fun initialize() {
        register<RenderEvent.Player.Pre> { event ->
            if (spineveryone || event.player == player) {
                GlStateManager.pushMatrix()
                GlStateManager.translate(event.x, event.y, event.z)
                GlStateManager.rotate(getRotation(), 0f, 1f, 0f)
                GlStateManager.translate(-event.x, -event.y, -event.z)
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