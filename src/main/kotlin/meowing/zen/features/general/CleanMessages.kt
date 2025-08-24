package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

@Zen.Module
object CleanGuildMessage : Feature("guildmessage") {
    private val guildPattern = Pattern.compile("Guild > (\\[.+?])? ?([a-zA-Z0-9_]+) ?(\\[.+?])?: (.+)")
    private val rankPattern = Pattern.compile("\\[(.+?)]")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Clean Chat", "Clean messages", ConfigElement(
                "guildmessage",
                "Clean guild messages",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.event.message.unformattedText.removeFormatting()
            val m = guildPattern.matcher(text)
            if (m.matches()) {
                event.cancel()
                val hrank = m.group(1) ?: ""
                val user = m.group(2) ?: ""
                val grank = m.group(3) ?: ""
                val msg = m.group(4) ?: ""
                val grankText = if (grank.isNotEmpty()) "§8$grank " else ""
                val formatted = "§2G §8> $grankText§${getRankColor(hrank)}$user§f: $msg"
                ChatUtils.addMessage(formatted)
            }
        }
    }

    private fun getRankColor(rank: String) = when {
        rank.isEmpty() -> "7"
        else -> when (rankPattern.matcher(rank).takeIf { it.find() }?.group(1)) {
            "Admin" -> "c"
            "Mod", "GM" -> "2"
            "Helper" -> "b"
            "MVP++", "MVP+", "MVP" -> if (rank.contains("++")) "6" else "b"
            "VIP+", "VIP" -> "a"
            else -> "7"
        }
    }
}

@Zen.Module
object CleanPartyMessage : Feature("partymessage") {
    private val partyPattern = Pattern.compile("Party > (\\[.+?])? ?(.+?): (.+)")
    private val rankPattern = Pattern.compile("\\[(.+?)]")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Clean Chat", "Clean messages", ConfigElement(
                "partymessage",
                "Clean party messages",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.event.message.unformattedText.removeFormatting()
            val m = partyPattern.matcher(text)
            if (m.matches()) {
                event.cancel()
                val hrank = m.group(1) ?: ""
                val user = m.group(2) ?: ""
                val msg = m.group(3) ?: ""
                val formatted = "§9P §8> §${getRankColor(hrank)}$user§f: $msg"
                ChatUtils.addMessage(formatted)
            }
        }
    }

    private fun getRankColor(rank: String) = when {
        rank.isEmpty() -> "7"
        else -> when (rankPattern.matcher(rank).takeIf { it.find() }?.group(1)) {
            "Admin" -> "c"
            "Mod", "GM" -> "2"
            "Helper" -> "b"
            "MVP++", "MVP+", "MVP" -> if (rank.contains("++")) "6" else "b"
            "VIP+", "VIP" -> "a"
            else -> "7"
        }
    }
}