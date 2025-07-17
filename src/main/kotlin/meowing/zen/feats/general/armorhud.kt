package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.config
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.LoopUtils.removeLoop
import meowing.zen.utils.TickUtils.loop
import meowing.zen.utils.Render2D
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.RenderGameOverlayEvent

@Zen.Module
object armorhud : Feature("armorhud") {
    private var armor = emptyList<ItemStack>()
    private var armorloop: Long = 0

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Armor HUD", ConfigElement(
                "armorhud",
                "Armor HUD",
                "Renders an overlay for your armor",
                ElementType.Switch(false)
            ))
            .addElement("General", "Armor HUD", ConfigElement(
                "armorhudvert",
                "Vertical",
                "Renders armor vertically instead of horizontally",
                ElementType.Switch(false),
                { config -> config["armorhud"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        val exampleText = if (config.armorhudvert) "[H]\n[C]\n[P]\n[B]" else "[H] [C] [P] [B]"
        
        HUDManager.register("Armor HUD", exampleText)

        armorloop = loop(20) {
            val player = mc.thePlayer ?: return@loop
            armor = player.inventory.armorInventory?.reversed() ?: emptyList()
        }

        register<RenderEvent.HUD> { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.TEXT && HUDManager.isEnabled("Armor HUD")) render()
        }
    }

    private fun render() {
        val x = HUDManager.getX("Armor HUD")
        val y = HUDManager.getY("Armor HUD")
        val scale = HUDManager.getScale("Armor HUD")

        val iconSize = 16f * scale
        val spacing = 2f * scale

        var currentX = x
        var currentY = y

        armor.forEach { item ->
            @Suppress("SENSELESS_COMPARISON")
            if (item != null) Render2D.renderItem(item, currentX, currentY, scale)
            if (config.armorhudvert) currentY += iconSize + spacing
            else currentX += iconSize + spacing
        }
    }

    override fun onUnregister() {
        if (armorloop != 0L) removeLoop(armorloop.toString())
        super.onUnregister()
    }
}