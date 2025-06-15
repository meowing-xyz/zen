package meowing.zen.feats.general

import meowing.zen.Zen
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

class cleanmsgs {
    companion object {
        private val guildPattern = Pattern.compile("Guild > (?:(\\[.+?])? ?([a-zA-Z0-9_]+) ?(\\[.+?])?): (.+)")
        private val partyPattern = Pattern.compile("Party > (?:(\\[.+?])? ?(.+?)): (.+)")
        private val rankPattern = Pattern.compile("\\[(.+?)]")

        @JvmStatic
        fun initialize() {
            Zen.registerListener("guildmsg", GuildMessage())
            Zen.registerListener("partymsg", PartyMessage())
        }
    }

    class GuildMessage {
        @SubscribeEvent
        fun onGuildChat(event: ClientChatReceivedEvent) = cleanmsgs().handleChat(event, guildPattern, "${DARK_GREEN}G ${DARK_GRAY}> ", true)
    }

    class PartyMessage {
        @SubscribeEvent
        fun onPartyChat(event: ClientChatReceivedEvent) = cleanmsgs().handleChat(event, partyPattern, "${BLUE}P ${DARK_GRAY}> ", false)
    }

    fun handleChat(event: ClientChatReceivedEvent, pattern: Pattern, prefix: String, hasGuildRank: Boolean) {
        if (event.type.toInt() != 0) return
        val m = pattern.matcher(event.message.unformattedText)
        if (m.matches()) {
            event.isCanceled = true
            val hrank = m.group(1) ?: ""
            val user = m.group(2) ?: ""
            val grank = if (hasGuildRank) m.group(3) ?: "" else ""
            val msg = if (hasGuildRank) m.group(4) ?: "" else m.group(3) ?: ""
            val grankText = if (grank.isNotEmpty()) "${DARK_GRAY}$grank " else ""
            Minecraft.getMinecraft().thePlayer?.addChatMessage(ChatComponentText("$prefix$grankText${getRankColor(hrank)}$user$WHITE: $msg"))
        }
    }

    fun getRankColor(rank: String) = when {
        rank.isEmpty() -> GRAY
        else -> when (rankPattern.matcher(rank).let { if (it.find()) it.group(1) else rank }) {
            "Admin" -> RED
            "Mod", "GM" -> DARK_GREEN
            "Helper" -> BLUE
            "MVP++", "MVP+", "MVP" -> if (rank.contains("++")) GOLD else AQUA
            "VIP+", "VIP" -> GREEN
            else -> GRAY
        }
    }
}