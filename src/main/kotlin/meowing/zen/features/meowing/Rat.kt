package meowing.zen.features.meowing
import meowing.zen.Zen
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

@Zen.Module
object Rat : Feature() {
    private val renderPos = BlockPos(-1, 72, -92)
    private val texture = ResourceLocation("zen", "rat.png")

    override fun initialize() {
        register<RenderEvent.World> { event ->
            render(event.partialTicks)
        }
    }

    private fun render(partialTicks: Float) {
        val player = mc.thePlayer ?: return
        val distance = renderPos.distanceSq(player.posX, player.posY, player.posZ)
        if (distance > 9216.0) return

        val interpX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks
        val interpY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks
        val interpZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks

        GlStateManager.pushMatrix()
        GlStateManager.pushAttrib()

        GlStateManager.translate(
            renderPos.x - interpX,
            renderPos.y - interpY,
            renderPos.z - interpZ
        )

        renderTextureOverlay()

        GlStateManager.popAttrib()
        GlStateManager.popMatrix()
    }



    private fun renderTextureOverlay() {
        mc.textureManager.bindTexture(texture)

        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.color(1f, 1f, 1f, 1f)

        val tess = Tessellator.getInstance()
        val renderer = tess.worldRenderer

        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        renderer.pos(0.0, 0.0, 0.001).tex(0.0, 1.0).endVertex()
        renderer.pos(1.0, 0.0, 0.001).tex(1.0, 1.0).endVertex()
        renderer.pos(1.0, 1.0, 0.001).tex(1.0, 0.0).endVertex()
        renderer.pos(0.0, 1.0, 0.001).tex(0.0, 0.0).endVertex()
        tess.draw()

        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
    }
}