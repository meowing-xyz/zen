package xyz.meowing.zen.features.dungeons

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

@Zen.Module
object TerminalTracker : Feature("termtracker", area = "catacombs") {
    private lateinit var completed: MutableMap<String, MutableMap<String, Int>>
    private val pattern = Pattern.compile("^(\\w{1,16}) (?:activated|completed) a (\\w+)! \\(\\d/\\d\\)$")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Terminal Tracker", ConfigElement(
                "termtracker",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        completed = mutableMapOf()
        register<ChatEvent.Receive> { event ->
            if (event.event.type.toInt() == 2) return@register
            val msg = event.event.message.unformattedText.removeFormatting()
            val matcher = pattern.matcher(msg)

            when {
                msg == "The Core entrance is opening!" -> {
                    completed.forEach { (user, data) ->
                        ChatUtils.addMessage("$prefix §b$user§7 - §b${data["lever"] ?: 0} §flevers §7| §b${data["terminal"] ?: 0} §fterminals §7| §b${data["device"] ?: 0} §fdevices")
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
        if (this::completed.isInitialized) completed.clear()
        super.onRegister()
    }

    override fun onUnregister() {
        if (this::completed.isInitialized) completed.clear()
        super.onUnregister()
    }
}