package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object termtracker {
    private val completed = mutableMapOf<String, MutableMap<String, Int>>()
    private val pattern = Pattern.compile("^(\\w{1,16}) (?:activated|completed) a (\\w+)! \\(\\d/\\d\\)$")

    @JvmStatic
    fun initialize() {
        Zen.registerListener("termtracker", this)
    }

    @SubscribeEvent
    fun onChatReceive(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return
        val msg = event.message.unformattedText.removeFormatting()
        val matcher = pattern.matcher(msg)

        if (matcher.matches()) {
            val user = matcher.group(1)
            val type = matcher.group(2)
            if (type in listOf("terminal", "lever", "device"))
                completed.getOrPut(user) { mutableMapOf() }[type] = (completed[user]?.get(type) ?: 0) + 1
        } else if (msg == "The Core entrance is opening!") {
            completed.forEach { (user, data) ->
                ChatUtils.addMessage("§c[Zen] §b$user§7 - §b${data["lever"] ?: 0} §flevers §7| §b${data["terminal"] ?: 0} §fterminals §7| §b${data["device"] ?: 0} §fdevices")
            }
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        completed.clear()
    }
}