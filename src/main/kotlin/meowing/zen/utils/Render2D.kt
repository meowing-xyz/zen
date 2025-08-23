package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack

object Render2D {
    private val fontObj = FontUtils.getFontRenderer()
    fun renderString(text: String, x: Float, y: Float, scale: Float, color: Int = 0xFFFFFF) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0.0f)
        GlStateManager.scale(scale, scale, 1.0f)
        fontObj.drawString(text, 0, 0, color)
        GlStateManager.popMatrix()
    }

    fun renderStringWithShadow(text: String, x: Float, y: Float, scale: Float, color: Int = 0xFFFFFF) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0.0f)
        GlStateManager.scale(scale, scale, 1.0f)
        fontObj.drawStringWithShadow(text, 0f, 0f, color)
        GlStateManager.popMatrix()
    }

    fun renderItem(item: ItemStack, x: Float, y: Float, scale: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0.0f)
        GlStateManager.scale(scale, scale, 1.0f)
        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(item, 0, 0)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.popMatrix()
    }

    fun String.width(): Int {
        val lines = split('\n')
        return lines.maxOf { mc.fontRendererObj.getStringWidth(it.removeFormatting()) }
    }

    fun String.height(): Int {
        val lineCount = count { it == '\n' } + 1
        return mc.fontRendererObj.FONT_HEIGHT * lineCount
    }
}