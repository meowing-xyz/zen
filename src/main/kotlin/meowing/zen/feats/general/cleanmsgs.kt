package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object cleanmsgs {
    private val guildPattern = Pattern.compile("Guild > (?:(\\[.+?])? ?([a-zA-Z0-9_]+) ?(\\[.+?])?): (.+)")
    private val partyPattern = Pattern.compile("Party > (?:(\\[.+?])? ?(.+?)): (.+)")
    private val rankPattern = Pattern.compile("\\[(.+?)]")

    @JvmStatic
    fun initialize() {
        Zen.registerListener("guildmsg", GuildMessage())
        Zen.registerListener("partymsg", PartyMessage())
    }

    private fun handleChat(
        event: ClientChatReceivedEvent,
        pattern: Pattern,
        prefix: String,
        hasGuildRank: Boolean
    ) {
        if (event.type.toInt() == 2) return
        val text = event.message.unformattedText.removeFormatting()
        val m = pattern.matcher(text)
        if (m.matches()) {
            event.isCanceled = true
            val hrank = m.group(1) ?: ""
            val user = m.group(2) ?: ""
            val grank = if (hasGuildRank) m.group(3) ?: "" else ""
            val msg = if (hasGuildRank) m.group(4) ?: "" else m.group(3) ?: ""
            val grankText = if (grank.isNotEmpty()) "§8$grank " else ""
            val formatted = "$prefix$grankText§${getRankColor(hrank)}$user§f: $msg"
            ChatUtils.addMessage(formatted)
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

    class GuildMessage {
        @SubscribeEvent
        fun onGuildChat(event: ClientChatReceivedEvent) = handleChat(event, guildPattern, "§2G §8> ", true)
    }

    class PartyMessage {
        @SubscribeEvent
        fun onPartyChat(event: ClientChatReceivedEvent) = handleChat(event, partyPattern, "§bP §8> ", false)
    }
}