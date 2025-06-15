package meowing.zen.feats

import meowing.zen.utils.*
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.random.Random

object automeow {
    private val regex = Pattern.compile("([A-Za-z0-9_.-]+).*:\\s.*meow$", Pattern.CASE_INSENSITIVE)
    private val meows = arrayOf("mroww", "purr", "meowwwwww", "meow :3", "mrow", "moew")
    private val channels = mapOf("Party >" to "pc", "Guild >" to "gc", "Officer >" to "oc", "Co-op >" to "cc")

     fun initialize() {
        TickScheduler.register()
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onChatReceived(event: ClientChatReceivedEvent) {
        val text = event.message.unformattedText
        val player = Minecraft.getMinecraft().thePlayer?.name

        if (event.type == 2.toByte() || !regex.matcher(text).matches() ||
            text.contains("To ") || text.contains(player ?: "")) return

        val cmd = if (text.startsWith("From ")) {
            regex.matcher(text).let { if (it.find()) "msg ${it.group(1)}" else return }
        } else channels.entries.find { text.startsWith(it.key) }?.value ?: "ac"

        TickScheduler.schedule(ceil(Random.nextDouble() * 40).toLong() + 10) {
            ChatUtils.command("$cmd ${meows.random()}")
        }
    }
}