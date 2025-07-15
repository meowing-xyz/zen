package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack

object Render2D {
    fun renderString(text: String, x: Float, y: Float, scale: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0.0f)
        GlStateManager.scale(scale, scale, 1.0f)
        mc.fontRendererObj.drawString(text, 0, 0, 0xFFFFFF)
        GlStateManager.popMatrix()
    }

    fun renderStringWithShadow(text: String, x: Float, y: Float, scale: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0.0f)
        GlStateManager.scale(scale, scale, 1.0f)
        mc.fontRendererObj.drawStringWithShadow(text, 0f, 0f, 0xFFFFFF)
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
}