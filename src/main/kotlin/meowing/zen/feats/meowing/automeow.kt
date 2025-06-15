package meowing.zen.feats.meowing

import meowing.zen.utils.TickScheduler
import meowing.zen.utils.ChatUtils
import meowing.zen.Zen
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.random.Random

class automeow {
    companion object {
        private val instance = automeow()

        @JvmStatic
        fun initialize() {
            Zen.registerListener("automeow", instance)
        }
    }
    private val regex = Pattern.compile("^(?:\\w+(?:-\\w+)?\\s>\\s)?(?:\\[[^]]+]\\s)?(?:\\S+\\s)?(?:\\[[^]]+]\\s)?([A-Za-z0-9_.-]+)(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?:\\s(?:[A-Za-z0-9_.-]+(?:\\s[^\\s\\[\\]:]+)?(?:\\s\\[[^]]+])?\\s?(?:[»>]|:)\\s)?meow$", Pattern.CASE_INSENSITIVE)
    private val meows = arrayOf("mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew")
    private val channels = mapOf("Party >" to "pc", "Guild >" to "gc", "Officer >" to "oc", "Co-op >" to "cc")

    @SubscribeEvent
    fun onChatReceived(event: ClientChatReceivedEvent) {
        val text = event.message.unformattedText
        val player = Minecraft.getMinecraft().thePlayer?.name
        if (event.type == 2.toByte() || !regex.matcher(text).matches() || text.contains("To ") || text.contains(player.toString())) return
        val cmd = if (text.startsWith("From ")) {
            regex.matcher(text).let {
                if (it.find())
                    "msg ${it.group(1)}"
                else return
            }
        } else channels.entries.find {
            text.startsWith(it.key)
        }?.value ?: "ac"
        TickScheduler.schedule(ceil(Random.nextDouble() * 40).toLong() + 10) {
            ChatUtils.command("$cmd ${meows.random()}")
        }
    }
}