package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.Zen
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.NetworkUtils
import xyz.meowing.zen.utils.Utils.canSeePosition
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.io.File
import javax.imageio.ImageIO

@Zen.Module
object Rat : Feature(area = "Hub") {
    private val renderPos = BlockPos(-1, 72, -92)
    private val textureLocation = ResourceLocation("zen", "zen_rat_png")
    private var textureLoaded = false

    override fun initialize() {
        loadTexture()
        register<RenderEvent.World> { event ->
            if (textureLoaded) {
                render(event.partialTicks)
            }
        }
    }

    private fun loadTexture() {
        val cacheFile = File(mc.mcDataDir, "cache/zen_rat.png")
        cacheFile.parentFile.mkdirs()

        NetworkUtils.downloadFile(
            url = "https://github.com/meowing-xyz/zen-data/raw/main/assets/rat.png",
            outputFile = cacheFile,
            onComplete = { file ->
                mc.addScheduledTask {
                    try {
                        val image = ImageIO.read(file)
                        val texture = DynamicTexture(image)
                        mc.textureManager.loadTexture(textureLocation, texture)
                        textureLoaded = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            onError = { error ->
                error.printStackTrace()
            }
        )
    }

    private fun render(partialTicks: Float) {
        val player = mc.thePlayer ?: return
        val distance = renderPos.distanceSq(player.posX, player.posY, player.posZ)
        if (distance > 9216.0) return

        val interpX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks
        val interpY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks
        val interpZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks

        if (!canSeePosition(Vec3(interpX, interpY, interpZ), renderPos)) return

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
        mc.textureManager.bindTexture(textureLocation)
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