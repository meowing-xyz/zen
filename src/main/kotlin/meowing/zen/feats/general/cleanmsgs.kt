package meowing.zen.feats.general

import meowing.zen.events.ChatReceiveEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

object guildmessage : Feature("guildmsg") {
    private val guildPattern = Pattern.compile("Guild > (?:(\\[.+?])? ?([a-zA-Z0-9_]+) ?(\\[.+?])?): (.+)")
    private val rankPattern = Pattern.compile("\\[(.+?)]")

    override fun initialize() {
        register<ChatReceiveEvent> { event ->
            if (event.event.type.toInt() == 2) return@register
            val text = event.event.message.unformattedText.removeFormatting()
            val m = guildPattern.matcher(text)
            if (m.matches()) {
                event.event.isCanceled = true
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

object partymessage : Feature("partymsg") {
    private val partyPattern = Pattern.compile("Party > (?:(\\[.+?])? ?(.+?)): (.+)")
    private val rankPattern = Pattern.compile("\\[(.+?)]")

    override fun initialize() {
        register<ChatReceiveEvent> { event ->
            if (event.event.type.toInt() == 2) return@register
            val text = event.event.message.unformattedText.removeFormatting()
            val m = partyPattern.matcher(text)
            if (m.matches()) {
                event.event.isCanceled = true
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