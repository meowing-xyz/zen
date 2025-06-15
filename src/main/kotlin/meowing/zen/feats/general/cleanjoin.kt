package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.utils.ChatUtils
import net.minecraft.util.EnumChatFormatting.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

class cleanjoin {
    companion object {
        private val guildPattern = Pattern.compile("^§2Guild > §r(§[a-f0-9])(\\w+) §r§e(\\w+)\\.§r$")
        private val friendPattern = Pattern.compile("^§aFriend > §r(§[a-f0-9])(\\w+) §r§e(\\w+)\\.§r$")

        @JvmStatic
        fun initialize() {
            Zen.registerListener("guildjoinleave", GuildJoinLeave())
            Zen.registerListener("friendjoinleave", FriendJoinLeave())
        }

        fun handleJoinLeave(event: ClientChatReceivedEvent, pattern: Pattern, prefix: String) {
            if (event.type.toInt() == 2) return
            val m = pattern.matcher(event.message.formattedText)
            if (m.matches()) {
                event.isCanceled = true
                val color = m.group(1) ?: ""
                val user = m.group(2) ?: ""
                val action = m.group(3) ?: ""

                val message = when (action) {
                    "joined" -> "${DARK_GRAY}$prefix ${GREEN}>> $color$user"
                    "left" -> "${DARK_GRAY}$prefix ${RED}<< $color$user"
                    else -> return
                }
                ChatUtils.addMessage(message)
            }
        }
    }

    class GuildJoinLeave {
        @SubscribeEvent
        fun onGuildJoinLeave(event: ClientChatReceivedEvent) = handleJoinLeave(event, guildPattern, "G")
    }

    class FriendJoinLeave {
        @SubscribeEvent
        fun onFriendJoinLeave(event: ClientChatReceivedEvent) = handleJoinLeave(event, friendPattern, "F")
    }
}