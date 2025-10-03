package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.DungeonUtils
import xyz.meowing.zen.utils.LocationUtils
import xyz.meowing.zen.utils.LoopUtils.setTimeout
import xyz.meowing.zen.utils.TitleUtils.showTitle
import xyz.meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object CryptReminder : Feature("cryptreminder", area = "catacombs") {
    private val cryptreminderdelay by ConfigDelegate<Double>("cryptreminderdelay")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Crypt reminder", "Options", ConfigElement(
                "cryptreminder",
                "Crypt reminder",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Dungeons", "Crypt reminder", "Options", ConfigElement(
                "cryptreminderdelay",
                "Crypt reminder delay",
                ElementType.Slider(1.0, 5.0, 2.0, false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Packet> { event ->
            if (event.packet.type.toInt() != 2 && event.packet.chatComponent.unformattedText.removeFormatting() == "[NPC] Mort: Good luck.") {
                setTimeout(1000 * 60 * cryptreminderdelay.toLong()) {
                    if (DungeonUtils.getCryptCount() == 5 || !LocationUtils.checkArea("catacombs")) return@setTimeout
                    ChatUtils.command("/pc Zen » ${DungeonUtils.getCryptCount()}/5 crypts")
                    showTitle("§c${DungeonUtils.getCryptCount()}§7/§c5 §fcrypts", null, 3000)
                }
            }
        }
    }
}