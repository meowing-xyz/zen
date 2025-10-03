package xyz.meowing.zen.ui.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.UMatrixStack
import xyz.meowing.zen.Zen.Companion.mc
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11
import java.awt.Color

class ItemComponent(var stack: ItemStack, var resolution: Float = 16f) : UIBlock() {
    override fun draw(matrixStack: UMatrixStack) {
        GlStateManager.pushMatrix()
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.enableDepth()
        GlStateManager.depthFunc(GL11.GL_LEQUAL)
        GlStateManager.translate(getLeft(), getTop(), 0f)
        GlStateManager.scale(resolution / 16f, resolution / 16f, 1f)

        mc.renderItem.renderItemIntoGUI(stack, 0, 0)
        mc.renderItem.renderItemOverlays(mc.fontRendererObj, stack, 0, 0)

        RenderHelper.disableStandardItemLighting()
        GlStateManager.popMatrix()
    }

    init {
        this.constrain {
            color = Color(0, 0, 0, 0).constraint
            width = resolution.pixels
            height = resolution.pixels
        }
    }
}