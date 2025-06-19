package meowing.zen.feats.general

import meowing.zen.Zen
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs

object customsize {
    private var x: Float = Zen.config.customX.toFloat()
    private var y: Float = Zen.config.customY.toFloat()
    private var z: Float = Zen.config.customZ.toFloat()

    @JvmStatic
    fun initialize() {
        Zen.registerListener("customsize", this)
        Zen.config.registerListener("customX") {
            x = Zen.config.customX.toFloat()
        }
        Zen.config.registerListener("customY") {
            y = Zen.config.customY.toFloat()
        }
        Zen.config.registerListener("customZ") {
            z = Zen.config.customZ.toFloat()
        }
    }

    @SubscribeEvent
    fun onRenderEntity(event: RenderLivingEvent.Pre<EntityPlayerSP>) {
        if (event.entity is EntityPlayerSP) {
            GlStateManager.pushMatrix()
            GlStateManager.scale(x, abs(y), z)
        }
    }

    @SubscribeEvent
    fun onPostRenderEntity(event: RenderLivingEvent.Post<EntityPlayerSP>) {
        if (event.entity is EntityPlayerSP)
            GlStateManager.popMatrix()
    }
}