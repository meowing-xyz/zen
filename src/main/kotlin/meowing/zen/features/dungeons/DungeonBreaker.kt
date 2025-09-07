package meowing.zen.features.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.PacketEvent
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.ItemUtils.lore
import meowing.zen.utils.ItemUtils.skyblockID
import meowing.zen.utils.Render2D
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.network.play.server.S2FPacketSetSlot

@Zen.Module
object DungeonBreaker : Feature("dungeonbreaker", "catacombs") {
    private const val name = "Dungeon Breaker Charges"
    private val regex = "Charges: (\\d+)/(\\d+)⸕".toRegex()
    private var charges = 0
    private var max = 0

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Breaker Charge Display", ConfigElement(
                "dungeonbreaker",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        HUDManager.register(name, "§bCharges: §e20§7/§e20§c⸕")

        register<PacketEvent.ReceivedPost> { event ->
            if (event.packet is S2FPacketSetSlot) {
                val stack = event.packet.func_149174_e() ?: return@register
                if (stack.skyblockID != "DUNGEONBREAKER") return@register

                stack.lore.firstNotNullOfOrNull { regex.find(it.removeFormatting()) }?.let { match ->
                    charges = match.groupValues[1].toIntOrNull() ?: 0
                    max = match.groupValues[2].toIntOrNull() ?: 0
                }
            }
        }

        register<RenderEvent.Text> {
            if (max == 0 || !HUDManager.isEnabled(name)) return@register
            val x = HUDManager.getX(name)
            val y = HUDManager.getY(name)
            val scale = HUDManager.getScale(name)

            Render2D.renderString("§bCharges: §e${charges}§7/§e${max}§c⸕", x, y, scale)
        }
    }
}