package meowing.zen.feats.general

import cc.polyfrost.oneconfig.hud.SingleTextHud
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack
import meowing.zen.Zen
import meowing.zen.events.PacketEvent
import meowing.zen.feats.Feature
import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.network.play.server.S30PacketWindowItems

class arrowpoisonhud : SingleTextHud("Arrow Poison", true) {
    override fun getText(example: Boolean): String = if (example) "64 | 32" else "${arrowpoison.twilight} | ${arrowpoison.toxic}"

    override fun shouldShow(): Boolean = Zen.config.arrowpoison && (arrowpoison.twilight > 0 || arrowpoison.toxic > 0)

    override fun draw(matrices: UMatrixStack, x: Float, y: Float, scale: Float, example: Boolean) {
        if (!example && !shouldShow()) return
        val twilightCount = if (example) 64 else arrowpoison.twilight
        val toxicCount = if (example) 32 else arrowpoison.toxic
        val iconSize = 16f * scale
        val spacing = 4f * scale

        val twilightPotion = ItemStack(Items.dye, 1, 5)
        val toxicPotion = ItemStack(Items.dye, 1, 10)
        val fontRenderer = mc.fontRendererObj

        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1f)

        val scaledX = (x / scale).toInt()
        val scaledY = (y / scale).toInt()
        val scaledTextY = (y + (iconSize - 8f * scale) / 2f) / scale

        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(twilightPotion, scaledX, scaledY)

        val twilightStr = twilightCount.toString()
        val twilightStrWidth = fontRenderer.getStringWidth(twilightStr)
        val textX1Scaled = (x + iconSize + spacing) / scale
        fontRenderer.drawString(twilightStr, textX1Scaled, scaledTextY, -1, true)

        val midXScaled = textX1Scaled + twilightStrWidth + (spacing * 2) / scale
        fontRenderer.drawString("|", midXScaled, scaledTextY, 0x888888, true)

        val toxicXScaled = midXScaled + fontRenderer.getStringWidth("|") + spacing / scale
        mc.renderItem.renderItemAndEffectIntoGUI(toxicPotion, toxicXScaled.toInt(), scaledY)

        val textX2Scaled = toxicXScaled + iconSize / scale + spacing / scale
        fontRenderer.drawString(toxicCount.toString(), textX2Scaled, scaledTextY, -1, true)

        RenderHelper.disableStandardItemLighting()
        GlStateManager.popMatrix()
    }

    override fun getWidth(scale: Float, example: Boolean): Float {
        val twilightCount = if (example) 64 else arrowpoison.twilight
        val toxicCount = if (example) 32 else arrowpoison.toxic
        val iconSize = 16f * scale
        val spacing = 4f * scale
        val fr = mc.fontRendererObj

        return iconSize * 2 + spacing * 6 +
                fr.getStringWidth(twilightCount.toString()) * scale +
                fr.getStringWidth(toxicCount.toString()) * scale +
                fr.getStringWidth("|") * scale
    }

    override fun getHeight(scale: Float, example: Boolean): Float = 16f * scale
}

object arrowpoison : Feature("arrowpoison") {
    var twilight = 0
    var toxic = 0
    override fun initialize() {
        register<PacketEvent.Received> { event ->
            if (event.packet is S2FPacketSetSlot || event.packet is S30PacketWindowItems) updateCount()
        }
    }

    private fun updateCount() {
        twilight = 0
        toxic = 0
        mc.thePlayer.inventory.mainInventory.forEach { item ->
            if (item == null) return@forEach
            val name = item.displayName.removeFormatting()
            if (name.contains("Twilight Arrow Poison")) twilight += item.stackSize
            if (name.contains("Toxic Arrow Poison")) toxic += item.stackSize
        }
    }
}