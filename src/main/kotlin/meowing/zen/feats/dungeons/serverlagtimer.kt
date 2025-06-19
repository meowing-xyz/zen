package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.events.ChatReceiveEvent
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.Utils.removeFormatting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object serverlagtimer {
    private val regex = Pattern.compile("^\\s*☠ Defeated .+ in 0?(?:[\\dhms ]+?)\\s*(?:\\(NEW RECORD!\\))?$")
    private var sent = false

    @JvmStatic
    fun initialize() {
        Zen.registerListener("serverlagtimer", this)
    }

    @SubscribeEvent
    fun onGameChat(event: ChatReceiveEvent) {
        val text = event.packet.chatComponent.unformattedText.removeFormatting()
        if (regex.matcher(text).matches() && !sent) {
            val lagtick = TickScheduler.getCurrentClientTick() - TickScheduler.getCurrentServerTick()
            val lagtime = lagtick / 20.0
            ChatUtils.addMessage("§c[Zen] §fServer lagged for §c${"%.1f".format(lagtime)}s §7| §c${lagtick} ticks§f.")
            sent = true
        }
    }
}