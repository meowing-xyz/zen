package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.DungeonUtils
import meowing.zen.utils.LoopUtils.setTimeout
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting

object cryptreminder : Feature("cryptreminder") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Crypt reminder", ConfigElement(
                "cryptreminder",
                "Crypt reminder",
                "Shows a notification about the current crypt count if all 5 aren't done",
                ElementType.Switch(false)
            ))
            .addElement("Dungeons", "Crypt reminder", ConfigElement(
                "cryptreminderdelay",
                "Crypt reminder delay",
                "Time in minutes",
                ElementType.Slider(1.0, 5.0, 2.0, false),
                { config -> config["cryptreminderdelay"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<ChatEvent.Packet> { event ->
            if (event.packet.type.toInt() != 2 && event.packet.chatComponent.unformattedText.removeFormatting() == "[NPC] Mort: Good luck.") {
                setTimeout(1000 * 60 * Zen.config.cryptreminderdelay.toLong()) {
                    if (DungeonUtils.getCryptCount() == 5) return@setTimeout
                    ChatUtils.command("/pc Zen » ${DungeonUtils.getCryptCount()}/5 crypts")
                    Utils.showTitle("§c${DungeonUtils.getCryptCount()}§7/§c5 §fcrypts", "", 1, 60, 1)
                }
            }
        }
    }
}