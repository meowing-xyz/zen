package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Utils.convertToFloat
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import kotlin.math.abs

object customsize : Feature("customsize") {
    private var x: Float = 1.0f
    private var y: Float = 1.0f
    private var z: Float = 1.0f

    override fun initialize() {
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