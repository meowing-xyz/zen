package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.events.RenderPlayerEvent
import meowing.zen.events.RenderPlayerPostEvent
import meowing.zen.feats.Feature
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import kotlin.math.abs

object customsize : Feature("customsize") {
    private var x: Float = 1.0f
    private var y: Float = 1.0f
    private var z: Float = 1.0f

    override fun initialize() {
        updateScaleValues()
        Zen.config.registerListener("customX") {
            updateScaleValues()
        }
        Zen.config.registerListener("customY") {
            updateScaleValues()
        }
        Zen.config.registerListener("customZ") {
            updateScaleValues()
        }

        register<RenderPlayerEvent> { event ->
            if (event.player is EntityPlayerSP) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(x, abs(y), z)
            }
        }

        register<RenderPlayerPostEvent> { event ->
            if (event.player is EntityPlayerSP) GlStateManager.popMatrix()
        }
    }

    private fun updateScaleValues() {
        x = Zen.config.customX.toFloat()
        y = Zen.config.customY.toFloat()
        z = Zen.config.customZ.toFloat()
    }
}