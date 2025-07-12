package meowing.zen.feats.general

import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.PacketEvent
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.network.play.server.S30PacketWindowItems
import net.minecraftforge.client.event.RenderGameOverlayEvent

object arrowpoison : Feature("arrowpoison") {
    private const val name = "ArrowPoison"
    private var twilight = 0
    private var toxic = 0

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Arrow poison tracker", ConfigElement(
                "arrowpoison",
                "Arrow poison tracker",
                "Tracks the arrow poisons inside your inventory.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        HUDManager.register(name, "<I> 64 | <I> 32")

        register<PacketEvent.Received> { event ->
            if (event.packet is S2FPacketSetSlot || event.packet is S30PacketWindowItems) updateCount()
        }

        register<RenderEvent.HUD> { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.TEXT) render()
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

    private fun render() {
        if (twilight == 0 && toxic == 0) return

        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        drawHUD(x, y, scale)
    }

    private fun drawHUD(x: Float, y: Float, scale: Float) {
        val iconSize = 16f * scale
        val spacing = 4f * scale
        val twilightPotion = ItemStack(Items.dye, 1, 5)
        val toxicPotion = ItemStack(Items.dye, 1, 10)
        val fontRenderer = mc.fontRendererObj

        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1f)
        val scaledX = (x / scale).toInt()
        val scaledY = (y / scale).toInt()

        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(twilightPotion, scaledX, scaledY)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.popMatrix()

        val twilightStr = twilight.toString()
        val textY = y + (iconSize - 8f * scale) / 2f
        val twilightTextX = x + iconSize + spacing
        Render2D.renderStringWithShadow(twilightStr, twilightTextX, textY, scale)

        val separatorX = twilightTextX + fontRenderer.getStringWidth(twilightStr) * scale + spacing * 2
        Render2D.renderStringWithShadow("§7|", separatorX, textY, scale)

        val toxicIconX = separatorX + fontRenderer.getStringWidth("|") * scale + spacing
        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1f)
        RenderHelper.enableGUIStandardItemLighting()
        mc.renderItem.renderItemAndEffectIntoGUI(toxicPotion, (toxicIconX / scale).toInt(), scaledY)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.popMatrix()

        val toxicTextX = toxicIconX + iconSize + spacing
        Render2D.renderStringWithShadow(toxic.toString(), toxicTextX, textY, scale)
    }
}