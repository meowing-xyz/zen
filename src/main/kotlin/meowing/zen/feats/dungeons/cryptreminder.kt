package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.events.ChatPacketEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.DungeonUtils
import meowing.zen.utils.LoopUtils.setTimeout
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting

object cryptreminder : Feature("cryptreminder") {
    override fun initialize() {
        register<ChatPacketEvent> { event ->
            if (event.packet.type.toInt() != 2 && event.packet.chatComponent.unformattedText.removeFormatting() == "[NPC] Mort: Good luck.") {
                ChatUtils.addMessage("eeeeeee")
                setTimeout(1000 * 60 * Zen.config.cryptreminderdelay.toLong()) {
                    if (DungeonUtils.getCryptCount() == 5) return@setTimeout
                    ChatUtils.command("/pc Zen » ${DungeonUtils.getCryptCount()}/5 crypts")
                    Utils.showTitle("§c${DungeonUtils.getCryptCount()}§7/§c5 §fcrypts", "", 1, 60, 1)
                }
            }
        }
    }
}