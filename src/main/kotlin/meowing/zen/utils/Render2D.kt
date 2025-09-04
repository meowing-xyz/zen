package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object Render2D {
    private val fontObj = FontUtils.getFontRenderer()
    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer

    fun renderString(text: String, x: Float, y: Float, scale: Float, color: Int = 0xFFFFFF) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
        GlStateManager.scale(scale, scale, 1f)
        fontObj.drawString(text, 0, 0, color)
        GlStateManager.popMatrix()
    }

    fun renderStringWithShadow(text: String, x: Float, y: Float, scale: Float, color: Int = 0xFFFFFF) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
        GlStateManager.scale(scale, scale, 1f)
        fontObj.drawStringWithShadow(text, 0f, 0f, color)
        GlStateManager.popMatrix()
    }

    fun renderItem(item: ItemStack, x: Float, y: Float, scale: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
        GlStateManager.scale(scale, scale, 1f)
        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(item, 0, 0)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.popMatrix()
    }

    fun preDraw() {
        GlStateManager.shadeModel(GL_SMOOTH)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.disableTexture2D()
        GlStateManager.disableCull()
        GlStateManager.disableLighting()
        GlStateManager.disableAlpha()
    }

    fun postDraw() {
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.enableCull()
        GlStateManager.enableAlpha()
        GlStateManager.resetColor()
        GlStateManager.shadeModel(GL_FLAT)
    }

    fun drawRoundedRect(color: Color, x: Number, y: Number, width: Number, height: Number, radius: Number = 5) {
        val xd = x.toDouble() * 2.0
        val yd = y.toDouble() * 2.0
        val widthd = width.toDouble() * 2.0
        val heightd = height.toDouble() * 2.0
        val radiusd = radius.toDouble() * 2.0
        GlStateManager.pushMatrix()
        GlStateManager.scale(0.5, 0.5, 0.5)
        preDraw()
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        val x1 = xd + radiusd
        val y1 = yd + radiusd
        val x2 = xd + widthd - radiusd
        val y2 = yd + heightd - radiusd
        worldRenderer.begin(GL_POLYGON, DefaultVertexFormats.POSITION)
        for (i in 180 .. 270 step 3) {
            val angle = i * PI / 180
            worldRenderer.pos(x1 + sin(angle) * radiusd, y1 + cos(angle) * radiusd, 0.0).endVertex()
        }
        for (i in 270 .. 360 step 3) {
            val angle = i * PI / 180
            worldRenderer.pos(x1 + sin(angle) * radiusd, y2 + cos(angle) * radiusd, 0.0).endVertex()
        }
        for (i in 0 .. 90 step 3) {
            val angle = i * PI / 180
            worldRenderer.pos(x2 + sin(angle) * radiusd, y2 + cos(angle) * radiusd, 0.0).endVertex()
        }
        for (i in 90 .. 180 step 3) {
            val angle = i * PI / 180
            worldRenderer.pos(x2 + sin(angle) * radiusd, y1 + cos(angle) * radiusd, 0.0).endVertex()
        }
        tessellator.draw()
        postDraw()
        GlStateManager.popMatrix()
    }

    fun drawRoundedRectLeft(color: Color, x: Number, y: Number, width: Number, height: Number, radius: Number = 5) {
        val xd = x.toDouble() * 2.0
        val yd = y.toDouble() * 2.0
        val widthd = width.toDouble() * 2.0
        val heightd = height.toDouble() * 2.0
        val radiusd = radius.toDouble() * 2.0
        GlStateManager.pushMatrix()
        GlStateManager.scale(0.5, 0.5, 0.5)
        preDraw()
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        val x1 = xd + radiusd
        val y1 = yd + radiusd
        val y2 = yd + heightd - radiusd
        worldRenderer.begin(GL_POLYGON, DefaultVertexFormats.POSITION)
        for (i in 180 .. 270 step 3) {
            val angle = i * PI / 180
            worldRenderer.pos(x1 + sin(angle) * radiusd, y1 + cos(angle) * radiusd, 0.0).endVertex()
        }
        for (i in 270 .. 360 step 3) {
            val angle = i * PI / 180
            worldRenderer.pos(x1 + sin(angle) * radiusd, y2 + cos(angle) * radiusd, 0.0).endVertex()
        }
        worldRenderer.pos(xd + widthd, yd + heightd, 0.0).endVertex()
        worldRenderer.pos(xd + widthd, yd, 0.0).endVertex()
        tessellator.draw()
        postDraw()
        GlStateManager.popMatrix()
    }

    fun drawRoundedRectRight(color: Color, x: Number, y: Number, width: Number, height: Number, radius: Number = 5) {
        val xd = x.toDouble() * 2.0
        val yd = y.toDouble() * 2.0
        val widthd = width.toDouble() * 2.0
        val heightd = height.toDouble() * 2.0
        val radiusd = radius.toDouble() * 2.0
        GlStateManager.pushMatrix()
        GlStateManager.scale(0.5, 0.5, 0.5)
        preDraw()
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        val x2 = xd + widthd - radiusd
        val y1 = yd + radiusd
        val y2 = yd + heightd - radiusd

        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(xd, yd, 0.0).endVertex()
        worldRenderer.pos(x2, yd, 0.0).endVertex()
        worldRenderer.pos(x2, yd + heightd, 0.0).endVertex()
        worldRenderer.pos(xd, yd + heightd, 0.0).endVertex()
        tessellator.draw()

        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(x2, y1, 0.0).endVertex()
        worldRenderer.pos(xd + widthd, y1, 0.0).endVertex()
        worldRenderer.pos(xd + widthd, y2, 0.0).endVertex()
        worldRenderer.pos(x2, y2, 0.0).endVertex()
        tessellator.draw()

        worldRenderer.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION)
        worldRenderer.pos(x2, y1, 0.0).endVertex()
        for (i in 90..180 step 3) {
            val angle = i * PI / 180
            worldRenderer.pos(x2 + sin(angle) * radiusd, y1 + cos(angle) * radiusd, 0.0).endVertex()
        }
        worldRenderer.pos(x2, yd, 0.0).endVertex()
        tessellator.draw()

        worldRenderer.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION)
        worldRenderer.pos(x2, y2, 0.0).endVertex()
        worldRenderer.pos(x2, yd + heightd, 0.0).endVertex()
        for (i in 0..90 step 3) {
            val angle = i * PI / 180
            worldRenderer.pos(x2 + sin(angle) * radiusd, y2 + cos(angle) * radiusd, 0.0).endVertex()
        }
        tessellator.draw()

        postDraw()
        GlStateManager.popMatrix()
    }

    fun drawRect(color: Color, x: Number, y: Number, width: Number, height: Number) {
        val xd = x.toDouble() * 2.0
        val yd = y.toDouble() * 2.0
        val widthd = width.toDouble() * 2.0
        val heightd = height.toDouble() * 2.0
        GlStateManager.pushMatrix()
        GlStateManager.scale(0.5, 0.5, 0.5)
        preDraw()
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        worldRenderer.pos(xd, yd, 0.0).endVertex()
        worldRenderer.pos(xd + widthd, yd, 0.0).endVertex()
        worldRenderer.pos(xd + widthd, yd + heightd, 0.0).endVertex()
        worldRenderer.pos(xd, yd + heightd, 0.0).endVertex()
        tessellator.draw()
        postDraw()
        GlStateManager.popMatrix()
    }

    fun String.width(): Int {
        val lines = split('\n')
        return lines.maxOf { fontObj.getStringWidth(it.removeFormatting()) }
    }

    fun String.height(): Int {
        val lineCount = count { it == '\n' } + 1
        return fontObj.FONT_HEIGHT * lineCount
    }
}