package meowing.zen.features.dungeons

import meowing.zen.Zen
import meowing.zen.api.PlayerStats
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D

@Zen.Module
object RoomSecrets : Feature("roomsecrets", "catacombs") {
    private const val name = "Secrets Display"

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Room Secrets Hud", ConfigElement(
                "roomsecrets",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        HUDManager.register(name, "§fSecrets: §a7§7/§a7")

        register<RenderEvent.HUD> { renderHUD() }
    }

    private fun renderHUD() {
        if (!HUDManager.isEnabled(name)) return

        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        Render2D.renderString(getText(), x, y, scale)
        // Eclipse was here :3
    }

    private fun getText(): String {
        val found = PlayerStats.currentRoomSecrets
        val total = PlayerStats.currentRoomMaxSecrets
        var text: String

        if ((found == 0 || found == -1) && total == 0) {
            text = "§fSecrets: §7None"
            return text
        }

        val percent = found.toFloat() / total.toFloat()

        text = when {
            percent < 0.5f -> "§fSecrets: §c$found§7/§c$total"
            percent < 1f -> "§fSecrets: §e$found§7/§e$total"
            else -> "§fSecrets: §a$found§7/§a$total"
        }

        return text
    }
}