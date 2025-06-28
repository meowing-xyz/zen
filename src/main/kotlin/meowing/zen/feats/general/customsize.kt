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
        val ConfigX = if (Zen.config.customX == "") "1" else Zen.config.customX
        val ConfigY = if (Zen.config.customY == "") "1" else Zen.config.customY
        val ConfigZ = if (Zen.config.customZ == "") "1" else Zen.config.customZ
        x = ConfigX.toFloat()
        y = ConfigY.toFloat()
        z = ConfigZ.toFloat()
    }
}