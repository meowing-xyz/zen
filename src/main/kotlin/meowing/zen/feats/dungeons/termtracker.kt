package meowing.zen.feats.dungeons

import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.events.ChatReceiveEvent
import meowing.zen.events.WorldUnloadEvent
import java.util.regex.Pattern

object termtracker : Feature("termtracker", area = "catacombs") {
    private val completed = mutableMapOf<String, MutableMap<String, Int>>()
    private val pattern = Pattern.compile("^(\\w{1,16}) (?:activated|completed) a (\\w+)! \\(\\d/\\d\\)$")

    override fun initialize() {
        register<ChatReceiveEvent> { event ->
            if (event.event.type.toInt() == 2) return@register
            val msg = event.event.message.unformattedText.removeFormatting()
            val matcher = pattern.matcher(msg)

            when {
                msg == "The Core entrance is opening!" -> {
                    completed.forEach { (user, data) ->
                        ChatUtils.addMessage("§c[Zen] §b$user§7 - §b${data["lever"] ?: 0} §flevers §7| §b${data["terminal"] ?: 0} §fterminals §7| §b${data["device"] ?: 0} §fdevices")
                    }
                }
                matcher.matches() -> {
                    val user = matcher.group(1)
                    val type = matcher.group(2)
                    if (type in listOf("terminal", "lever", "device"))
                        completed.getOrPut(user) { mutableMapOf() }[type] = (completed[user]?.get(type) ?: 0) + 1
                }
            }
        }
    }

    override fun onRegister() {
        completed.clear()
    }

    override fun onUnregister() {
        completed.clear()
    }
}