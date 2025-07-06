package meowing.zen.hud

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils
import net.minecraft.client.gui.Gui.drawRect
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color
import kotlin.math.pow

class HUDElement(
    val name: String,
    initialX: Float,
    initialY: Float,
    val width: Int,
    val height: Int,
    val exampleText: String,
    var scale: Float = 1f,
    var enabled: Boolean = true
) {
    private var currentX = initialX
    private var currentY = initialY
    var targetX = initialX
    var targetY = initialY
    private var lastUpdateTime = System.currentTimeMillis()

    fun setPosition(x: Float, y: Float) {
        currentX = getRenderX(Utils.getPartialTicks())
        currentY = getRenderY(Utils.getPartialTicks())
        targetX = x
        targetY = y
        lastUpdateTime = System.currentTimeMillis()
    }

    fun getRenderX(partialTicks: Float): Float {
        val timeDiff = (System.currentTimeMillis() - lastUpdateTime) / 1000f
        val progress = (timeDiff * 8f).coerceIn(0f, 1f)
        return currentX + (targetX - currentX) * easeOutCubic(progress)
    }

    fun getRenderY(partialTicks: Float): Float {
        val timeDiff = (System.currentTimeMillis() - lastUpdateTime) / 1000f
        val progress = (timeDiff * 8f).coerceIn(0f, 1f)
        return currentY + (targetY - currentY) * easeOutCubic(progress)
    }

    private fun easeOutCubic(t: Float) = 1f + (t - 1f).pow(3)

    fun render(mouseX: Float, mouseY: Float, partialTicks: Float, previewMode: Boolean) {
        if (!enabled && previewMode) return

        val renderX = getRenderX(partialTicks)
        val renderY = getRenderY(partialTicks)
        val isHovered = isMouseOver(mouseX, mouseY)

        GlStateManager.pushMatrix()
        GlStateManager.translate(renderX + width / 2, renderY + height / 2, 0f)
        GlStateManager.scale(scale, scale, 1f)
        GlStateManager.translate(-width / 2.0, -height / 2.0, 0.0)

        if (!previewMode) {
            val alpha = if (!enabled) 40 else if (isHovered) 140 else 90
            val borderColor = when {
                !enabled -> Color(200, 60, 60).rgb
                isHovered -> Color(100, 180, 255).rgb
                else -> Color(100, 100, 120).rgb
            }

            drawRect(0, 0, width, height, Color(30, 35, 45, alpha).rgb)
            drawHollowRect(0, 0, width, height, borderColor)
        }

        val lines = exampleText.split("\n")
        val textAlpha = if (enabled) 255 else 128
        val textColor = Color(220, 240, 255, textAlpha).rgb

        lines.forEachIndexed { index, line ->
            val textY = 5f + (index * mc.fontRendererObj.FONT_HEIGHT)
            mc.fontRendererObj.drawStringWithShadow(line, 5f, textY, textColor)
        }

        GlStateManager.popMatrix()
    }

    fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        val renderX = getRenderX(Utils.getPartialTicks())
        val renderY = getRenderY(Utils.getPartialTicks())
        val scaledWidth = width * scale
        val scaledHeight = height * scale
        val offsetX = (width - scaledWidth) / 2
        val offsetY = (height - scaledHeight) / 2

        return mouseX >= renderX + offsetX && mouseX <= renderX + offsetX + scaledWidth &&
                mouseY >= renderY + offsetY && mouseY <= renderY + offsetY + scaledHeight
    }

    private fun drawHollowRect(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        drawRect(x1, y1, x2, y1 + 1, color)
        drawRect(x1, y2 - 1, x2, y2, color)
        drawRect(x1, y1, x1 + 1, y2, color)
        drawRect(x2 - 1, y1, x2, y2, color)
    }
}