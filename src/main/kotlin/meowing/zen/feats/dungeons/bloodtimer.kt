package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.events.ChatReceiveEvent
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object bloodtimer {
    private val bloodstart = Pattern.compile("\\[BOSS] The Watcher: .+")
    private val dialogue = Pattern.compile("\\[BOSS] The Watcher: Let's see how you can handle this\\.")
    private val bloodcamp = Pattern.compile("\\[BOSS] The Watcher: You have proven yourself\\. You may pass\\.")

    private var bloodopen = false
    private var starttime: Long = 0

    @JvmStatic
    fun initialize() {
        Zen.registerListener("bloodtimer", this)
    }

    @SubscribeEvent
    fun onChatReceive(event: ChatReceiveEvent) {
        val text = event.packet.chatComponent.unformattedText.removeFormatting()
        when {
            !bloodopen && bloodstart.matcher(text).matches() -> {
                bloodopen = true
                starttime = System.currentTimeMillis()
            }
            dialogue.matcher(text).matches() -> {
                val diftime = (System.currentTimeMillis() - starttime) / 1000.0
                Utils.showTitle("§c§l!", "§cWatcher reached dialogue!", 125)
                ChatUtils.addMessage("§c[Zen] §fWatcher took §c${"%.2f".format(diftime)}s §fto reach dialogue!")
            }
            bloodcamp.matcher(text).matches() -> {
                val camptime = (System.currentTimeMillis() - starttime) / 1000.0
                ChatUtils.addMessage("§c[Zen] §fBlood camp took §C${"%.2f".format(camptime)}s")
            }
        }
    }
}