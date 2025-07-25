package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.PacketEvent
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.network.play.server.S30PacketWindowItems
import net.minecraftforge.client.event.RenderGameOverlayEvent

@Zen.Module
object ArrowPoison : Feature("arrowpoison") {
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
        HUDManager.registerCustom(name, 85, 17, this::HUDEditorRender)

        register<PacketEvent.Received> { event ->
            if (event.packet is S2FPacketSetSlot || event.packet is S30PacketWindowItems) updateCount()
        }

        register<RenderEvent.HUD> { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.TEXT && HUDManager.isEnabled("ArrowPoison")) render()
        }
    }

    private fun updateCount() {
        twilight = 0
        toxic = 0
        val inventory = player?.inventory?.mainInventory ?: return
        inventory.forEach { item ->
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
        drawHUD(x, y, scale, false)
    }

    @Suppress("UNUSED")
    private fun HUDEditorRender(x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean) {
        drawHUD(x, y, 1f, true)
    }

    private fun drawHUD(x: Float, y: Float, scale: Float, preview: Boolean) {
        val iconSize = 16f * scale
        val spacing = 4f * scale
        val twilightPotion = ItemStack(Items.dye, 1, 5)
        val toxicPotion = ItemStack(Items.dye, 1, 10)
        val twilightStr = if (preview) "128" else twilight.toString()
        val toxicStr = if (preview) "92" else toxic.toString()
        val textY = y + (iconSize - 8f) / 2f
        var currentX = x

        Render2D.renderItem(twilightPotion, currentX, y, scale)
        currentX += iconSize + spacing
        Render2D.renderStringWithShadow(twilightStr, currentX, textY, scale)

        currentX += fontRenderer.getStringWidth(twilightStr) * scale + spacing * 2
        Render2D.renderStringWithShadow("§7|", currentX, textY, scale)

        currentX += fontRenderer.getStringWidth("|") * scale + spacing
        Render2D.renderItem(toxicPotion, currentX, y, scale)

        currentX += iconSize + spacing
        Render2D.renderStringWithShadow(toxicStr, currentX, textY, scale)
    }
}