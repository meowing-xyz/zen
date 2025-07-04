package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.util.Vector3d
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object RenderUtils {
    private val renderManager = mc.renderManager
    
    fun drawOutlineBox(entity: Entity, color: Color, partialTicks: Float, lineWidth: Float = 2.0f) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - renderManager.viewerPosX
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - renderManager.viewerPosY
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - renderManager.viewerPosZ

        val boundingBox = AxisAlignedBB(
            x - entity.width / 2, y, z - entity.width / 2,
            x + entity.width / 2, y + entity.height, z + entity.width / 2
        )

        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

        GL11.glLineWidth(lineWidth)
        GlStateManager.color(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        tessellator.draw()

        GlStateManager.enableLighting()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawOutlineBox(x: Double, y: Double, z: Double, width: Float, height: Float, color: Color, lineWidth: Float = 2.0f) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        val renderX = x - renderManager.viewerPosX
        val renderY = y - renderManager.viewerPosY
        val renderZ = z - renderManager.viewerPosZ

        val boundingBox = AxisAlignedBB(
            renderX - width / 2, renderY, renderZ - width / 2,
            renderX + width / 2, renderY + height, renderZ + width / 2
        )

        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

        GL11.glLineWidth(lineWidth)
        GlStateManager.color(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)

        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        tessellator.draw()

        GlStateManager.enableLighting()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun renderBlock(blockPosition: BlockPos, partialTicks: Float, fill: Boolean, color: Color, lineWidth: Float) {
        val world = mc.theWorld
        val blockState = world.getBlockState(blockPosition)
        val block = blockState.block
        val boundingBox = block.getSelectedBoundingBox(world, blockPosition)
            ?: AxisAlignedBB(blockPosition.x.toDouble(), blockPosition.y.toDouble(), blockPosition.z.toDouble(), blockPosition.x + 1.0, blockPosition.y + 1.0, blockPosition.z + 1.0)
        val player = mc.thePlayer
        val interpolatedX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks
        val interpolatedY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks
        val interpolatedZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks

        val minX = boundingBox.minX - interpolatedX
        val minY = boundingBox.minY - interpolatedY
        val minZ = boundingBox.minZ - interpolatedZ
        val maxX = boundingBox.maxX - interpolatedX
        val maxY = boundingBox.maxY - interpolatedY
        val maxZ = boundingBox.maxZ - interpolatedZ

        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GlStateManager.disableDepth()
        GlStateManager.disableCull()

        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        if (fill) {
            GlStateManager.color(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
            worldRenderer.pos(minX, minY, minZ).endVertex()
            worldRenderer.pos(minX, maxY, minZ).endVertex()
            worldRenderer.pos(maxX, maxY, minZ).endVertex()
            worldRenderer.pos(maxX, minY, minZ).endVertex()
            worldRenderer.pos(maxX, minY, maxZ).endVertex()
            worldRenderer.pos(maxX, maxY, maxZ).endVertex()
            worldRenderer.pos(minX, maxY, maxZ).endVertex()
            worldRenderer.pos(minX, minY, maxZ).endVertex()
            worldRenderer.pos(minX, minY, minZ).endVertex()
            worldRenderer.pos(minX, minY, maxZ).endVertex()
            worldRenderer.pos(minX, maxY, maxZ).endVertex()
            worldRenderer.pos(minX, maxY, minZ).endVertex()
            worldRenderer.pos(maxX, maxY, minZ).endVertex()
            worldRenderer.pos(maxX, maxY, maxZ).endVertex()
            worldRenderer.pos(maxX, minY, maxZ).endVertex()
            worldRenderer.pos(maxX, minY, minZ).endVertex()
            worldRenderer.pos(minX, minY, minZ).endVertex()
            worldRenderer.pos(maxX, minY, minZ).endVertex()
            worldRenderer.pos(maxX, minY, maxZ).endVertex()
            worldRenderer.pos(minX, minY, maxZ).endVertex()
            worldRenderer.pos(minX, maxY, maxZ).endVertex()
            worldRenderer.pos(maxX, maxY, maxZ).endVertex()
            worldRenderer.pos(maxX, maxY, minZ).endVertex()
            worldRenderer.pos(minX, maxY, minZ).endVertex()
            tessellator.draw()
        }

        GL11.glLineWidth(lineWidth)
        GlStateManager.color(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)
        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)
        worldRenderer.pos(minX, minY, minZ).endVertex()
        worldRenderer.pos(maxX, minY, minZ).endVertex()
        worldRenderer.pos(maxX, minY, minZ).endVertex()
        worldRenderer.pos(maxX, minY, maxZ).endVertex()
        worldRenderer.pos(maxX, minY, maxZ).endVertex()
        worldRenderer.pos(minX, minY, maxZ).endVertex()
        worldRenderer.pos(minX, minY, maxZ).endVertex()
        worldRenderer.pos(minX, minY, minZ).endVertex()
        worldRenderer.pos(minX, maxY, minZ).endVertex()
        worldRenderer.pos(maxX, maxY, minZ).endVertex()
        worldRenderer.pos(maxX, maxY, minZ).endVertex()
        worldRenderer.pos(maxX, maxY, maxZ).endVertex()
        worldRenderer.pos(maxX, maxY, maxZ).endVertex()
        worldRenderer.pos(minX, maxY, maxZ).endVertex()
        worldRenderer.pos(minX, maxY, maxZ).endVertex()
        worldRenderer.pos(minX, maxY, minZ).endVertex()
        worldRenderer.pos(minX, minY, minZ).endVertex()
        worldRenderer.pos(minX, maxY, minZ).endVertex()
        worldRenderer.pos(maxX, minY, minZ).endVertex()
        worldRenderer.pos(maxX, maxY, minZ).endVertex()
        worldRenderer.pos(maxX, minY, maxZ).endVertex()
        worldRenderer.pos(maxX, maxY, maxZ).endVertex()
        worldRenderer.pos(minX, minY, maxZ).endVertex()
        worldRenderer.pos(minX, maxY, maxZ).endVertex()
        tessellator.draw()

        GlStateManager.enableCull()
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun renderString(
        text: String,
        pos: Vec3,
        color: Int,
        scale: Float = 1.0f,
        yOffset: Float = 0.0f
    ) {
        val renderManager = mc.renderManager

        val x = (pos.xCoord - renderManager.viewerPosX).toFloat()
        val y = (pos.yCoord - renderManager.viewerPosY + yOffset).toFloat()
        val z = (pos.zCoord - renderManager.viewerPosZ).toFloat()

        val depthFunc = glGetInteger(GL_DEPTH_FUNC)
        val depthTest = glIsEnabled(GL_DEPTH_TEST)

        glEnable(GL_DEPTH_TEST)
        glDepthFunc(GL_ALWAYS)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_LIGHTING)

        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, z)
        GlStateManager.rotate(-renderManager.playerViewY, 0f, 1f, 0f)
        GlStateManager.rotate(renderManager.playerViewX, 1f, 0f, 0f)

        val textScale = scale * 0.025f
        GlStateManager.scale(-textScale, -textScale, textScale)

        val fontRenderer = mc.fontRendererObj
        val textWidth = fontRenderer.getStringWidth(text)
        fontRenderer.drawString(text, (-textWidth / 2).toFloat(), 0f, color, false)

        GlStateManager.popMatrix()

        glDepthFunc(depthFunc)
        if (!depthTest) glDisable(GL_DEPTH_TEST)
        glEnable(GL_LIGHTING)
    }
}