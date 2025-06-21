package meowing.zen.feats.dungeons

import cc.polyfrost.oneconfig.hud.TextHud
import meowing.zen.feats.Feature
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.events.ChatReceiveEvent
import meowing.zen.events.ServerTickEvent

object firefreeze : Feature("firefreeze", area = "catacombs") {
    var ticks = 0
    private var ticking = false

    override fun initialize() {
        register<ChatReceiveEvent> { event ->
            if (event.event.type.toInt() == 2) return@register
            if (event.event.message.unformattedText.removeFormatting() == "[BOSS] The Professor: Oh? You found my Guardians' one weakness?") {
                ticks = 100
                ticking = true
                TickScheduler.scheduleServer(105) {
                    Utils.playSound("random.anvil_land", 1f, 0.5f)
                    ticks = 0
                    ticking = false
                }
            }
        }

        register<ServerTickEvent> { event ->
            if (ticking && ticks > 0) {
                ticks--
                if (ticks <= 0) ticking = false
            }
        }
    }

    override fun onRegister() {
        ticks = 0
        ticking = false
    }

    override fun onUnregister() {
        ticks = 0
        ticking = false
    }
}

class FireFreezeHud : TextHud(true, 200, 100) {
    override fun getLines(lines: MutableList<String>, example: Boolean) {
        if (example) {
            lines.add("§bFire freeze: §c4.3s")
            return
        }

        if (firefreeze.ticks > 0) lines.add("§bFire freeze: §c${"%.1f".format(firefreeze.ticks / 20.0)}s")
    }
}