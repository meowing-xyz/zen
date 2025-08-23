package meowing.zen.utils

import gg.essential.elementa.utils.withAlpha
import meowing.zen.Zen.Companion.mc
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object Render3D {
    private val renderManager = mc.renderManager
    private val fontObj = FontUtils.getFontRenderer()
    
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
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)

        glLineWidth(lineWidth)
        GlStateManager.color(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL_LINES, DefaultVertexFormats.POSITION)
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
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)

        glLineWidth(lineWidth)
        GlStateManager.color(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)

        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(GL_LINES, DefaultVertexFormats.POSITION)
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

    fun renderBlock(blockPosition: BlockPos, partialTicks: Float, fill: Boolean, color: Color, lineWidth: Float, phase: Boolean = true) {
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

        if (phase) GlStateManager.disableDepth() else GlStateManager.enableDepth()

        GlStateManager.disableCull()

        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        if (fill) {
            GlStateManager.color(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)
            worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
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

        glLineWidth(lineWidth)
        GlStateManager.color(color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f, color.alpha / 255.0f)
        worldRenderer.begin(GL_LINES, DefaultVertexFormats.POSITION)
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

    fun drawString(
        text: String,
        pos: Vec3,
        partialTicks: Float,
        depth: Boolean = false,
        scale: Float = 1.0f,
    ) {
        val player = mc.thePlayer
        val viewerPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks.toDouble()
        val viewerPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks.toDouble()
        val viewerPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks.toDouble()

        val posX = pos.xCoord - viewerPosX
        val posY = pos.yCoord - viewerPosY
        val posZ = pos.zCoord - viewerPosZ

        GlStateManager.pushMatrix()
        GlStateManager.translate(posX.toFloat(), posY.toFloat(), posZ.toFloat())
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        val textScale = scale * 0.025f
        GlStateManager.scale(-textScale, -textScale, textScale)

        GlStateManager.disableLighting()

        if (!depth) {
            GlStateManager.depthMask(false)
            GlStateManager.disableDepth()
        }

        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)

        val width = fontObj.getStringWidth(text) / 2.0f
        fontObj.drawString(text, (-width), 0f, 0xFFFFFF, true)

        if (!depth) {
            GlStateManager.enableDepth()
            GlStateManager.depthMask(true)
        }

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.enableLighting()

        GlStateManager.popMatrix()
    }

    fun drawLineToEntity(entity: Entity, thickness: Float, color: Color, partialTicks: Float) {
        val player = mc.thePlayer ?: return
        if (!player.canEntityBeSeen(entity)) return

        val entityPos = entity.positionVector.add(Vec3(0.0, entity.eyeHeight.toDouble(), 0.0))
        drawLineToPos(entityPos, thickness, color, partialTicks)
    }

    fun drawLineToPos(pos: Vec3, thickness: Float, color: Color, partialTicks: Float) {
        val player = mc.thePlayer ?: return
        val playerPos = player.getPositionEyes(partialTicks)
        val toTarget = pos.subtract(playerPos).normalize()
        val lookVec = player.getLook(partialTicks).normalize()

        if (toTarget.dotProduct(lookVec) < 0.3) return

        val result = player.worldObj.rayTraceBlocks(playerPos, pos, false, true, false)
        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) return

        drawLine(playerPos, pos, thickness, color, partialTicks)
    }

    fun drawLine(from: Vec3, to: Vec3, thickness: Float, color: Color, partialTicks: Float) {
        val player = mc.thePlayer ?: return
        val viewerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks
        val viewerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks
        val viewerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks

        GlStateManager.pushMatrix()
        GlStateManager.translate(-viewerX, -viewerY, -viewerZ)

        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GlStateManager.disableDepth()

        glLineWidth(thickness)
        glBegin(GL_LINES)
        glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        glVertex3d(from.xCoord, from.yCoord, from.zCoord)
        glVertex3d(to.xCoord, to.yCoord, to.zCoord)
        glEnd()

        GlStateManager.enableDepth()
        GlStateManager.enableCull()
        GlStateManager.enableLighting()
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    fun drawFilledCircle(center: Vec3, radius: Float, segments: Int, borderColor: Color, fillColor: Color, partialTicks: Float) {
        val player = mc.thePlayer ?: return

        val interpolatedX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks
        val interpolatedY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks
        val interpolatedZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks

        val centerX = center.xCoord - interpolatedX
        val centerY = center.yCoord - interpolatedY + 0.01
        val centerZ = center.zCoord - interpolatedZ

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        GlStateManager.disableLighting()
        GlStateManager.disableCull()

        GlStateManager.color(
            fillColor.red / 255f,
            fillColor.green / 255f,
            fillColor.blue / 255f,
            fillColor.alpha / 255f
        )
        glBegin(GL_TRIANGLE_FAN)
        glVertex3d(centerX, centerY, centerZ)
        for (i in 0..segments) {
            val angle = Math.PI * 2 * i / segments
            val x = radius * cos(angle)
            val z = radius * sin(angle)
            glVertex3d(centerX + x, centerY, centerZ + z)
        }
        glEnd()

        GlStateManager.color(
            borderColor.red / 255f,
            borderColor.green / 255f,
            borderColor.blue / 255f,
            borderColor.alpha / 255f
        )
        glLineWidth(3f)
        glBegin(GL_LINE_LOOP)
        for (i in 0..segments) {
            val angle = Math.PI * 2 * i / segments
            val x = radius * cos(angle)
            val z = radius * sin(angle)
            glVertex3d(centerX + x, centerY, centerZ + z)
        }
        glEnd()

        GlStateManager.depthFunc(GL_LEQUAL)
        GlStateManager.enableCull()
        GlStateManager.enableLighting()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawSpecialBB(pos: BlockPos, fillColor: Color, partialTicks: Float) {
        val bb = AxisAlignedBB(pos, pos.add(1, 1, 1)).offset(-0.001, -0.001, -0.001).expand(0.002, 0.002, 0.002)
        drawSpecialBB(bb, fillColor, partialTicks)
    }

    fun drawSpecialBB(bb: AxisAlignedBB, fillColor: Color, partialTicks: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)

        val width = max(1 - (mc.thePlayer.getDistance(bb.minX, bb.minY, bb.minZ) / 10 - 2), 2.0)
        drawFilledBB(bb, fillColor.withAlpha(0.6f), partialTicks)
        drawOutlinedBB(bb, fillColor.withAlpha(0.9f), width.toFloat(), partialTicks)
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawOutlinedBB(aabbbb: AxisAlignedBB?, color: Color, width: Float, partialTicks: Float) {
        val render = mc.renderViewEntity
        val realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks
        val realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks
        val realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks
        GlStateManager.pushMatrix()
        GlStateManager.translate(-realX, -realY, -realZ)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.disableTexture2D()
        glLineWidth(width)
        RenderGlobal.drawOutlinedBoundingBox(aabbbb, color.red, color.green, color.blue, color.alpha)
        GlStateManager.translate(realX, realY, realZ)
        GlStateManager.popMatrix()
    }

    fun drawFilledBB(bb: AxisAlignedBB, c: Color, partialTicks: Float, customAlpha: Float = 0.15f) {
        val aabb = bb.offset(-0.002, -0.001, -0.002).expand(0.004, 0.005, 0.004)
        val render = mc.renderViewEntity
        val realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks
        val realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks
        val realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks
        GlStateManager.pushMatrix()
        GlStateManager.translate(-realX, -realY, -realZ)
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        val color = c.rgb
        var a = (color shr 24 and 255).toFloat() / 255.0f
        a = (a.toDouble() * customAlpha).toFloat()
        val r = (color shr 16 and 255).toFloat() / 255.0f
        val g = (color shr 8 and 255).toFloat() / 255.0f
        val b = (color and 255).toFloat() / 255.0f
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        tessellator.draw()
        GlStateManager.translate(realX, realY, realZ)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.popMatrix()
    }
}