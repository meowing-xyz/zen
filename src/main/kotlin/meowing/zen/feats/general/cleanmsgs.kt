package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.utils.ChatUtils
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting.*
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

    fun handleChat(event: ClientChatReceivedEvent, pattern: Pattern, prefix: String, hasGuildRank: Boolean) {
        if (event.type.toInt() == 2) return
        val text = ChatUtils.removeFormatting(event.message.unformattedText)
        val m = pattern.matcher(text)
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

    class GuildMessage {
        @SubscribeEvent
        fun onGuildChat(event: ClientChatReceivedEvent) = handleChat(event, guildPattern, "${DARK_GREEN}G ${DARK_GRAY}> ", true)
    }

    class PartyMessage {
        @SubscribeEvent
        fun onPartyChat(event: ClientChatReceivedEvent) = handleChat(event, partyPattern, "${BLUE}P ${DARK_GRAY}> ", false)
    }
}