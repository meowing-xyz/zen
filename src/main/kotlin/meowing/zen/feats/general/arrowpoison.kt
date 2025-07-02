package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.PacketEvent
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDEditor
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.network.play.server.S30PacketWindowItems
import net.minecraftforge.client.event.RenderGameOverlayEvent

object arrowpoison : Feature("arrowpoison") {
    var twilight = 0
    var toxic = 0

    override fun initialize() {
        HUDManager.registerElement("ArrowPoison", "64 | 32")

        register<PacketEvent.Received> { event ->
            if (event.packet is S2FPacketSetSlot || event.packet is S30PacketWindowItems) updateCount()
        }

        register<RenderEvent.HUD> { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.TEXT) ArrowPoisonHUD.render()
        }
    }

    private fun updateCount() {
        twilight = 0
        toxic = 0
        if (mc.thePlayer == null || mc.thePlayer.inventory.mainInventory == null) return
        mc.thePlayer.inventory.mainInventory.forEach { item ->
            if (item == null) return@forEach
            val name = item.displayName.removeFormatting()
            if (name.contains("Twilight Arrow Poison")) twilight += item.stackSize
            if (name.contains("Toxic Arrow Poison")) toxic += item.stackSize
        }
    }
}

object ArrowPoisonHUD {
    private const val name = "ArrowPoison"

    fun render() {
        if (!Zen.config.arrowpoison || (arrowpoison.twilight == 0 && arrowpoison.toxic == 0)) return

        val x = HUDEditor.getX(name)
        val y = HUDEditor.getY(name)
        val scale = 1f // TODO: Make it customisable

        drawHUD(x, y, scale, false)
    }

    private fun drawHUD(x: Float, y: Float, scale: Float, example: Boolean) {
        val twilightCount = if (example) 64 else arrowpoison.twilight
        val toxicCount = if (example) 32 else arrowpoison.toxic
        val iconSize = 16f * scale
        val spacing = 4f * scale

        val twilightPotion = ItemStack(Items.dye, 1, 5) // Purple dye
        val toxicPotion = ItemStack(Items.dye, 1, 10) // Lime dye
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
}