package meowing.zen.utils

import meowing.zen.Zen.Companion.mc
import meowing.zen.Zen.Companion.prefix
import meowing.zen.events.EventBus
import meowing.zen.features.Debug
import meowing.zen.utils.TimeUtils.millis
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraftforge.client.ClientCommandHandler
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds

object ChatUtils {
    private var nextAvailableTime = TimeUtils.zero

    private fun schedule(action: () -> Unit) {
        val now = TimeUtils.now
        nextAvailableTime = maxOf(now, nextAvailableTime)
        val delay = nextAvailableTime.until.millis / 50.0

        TickUtils.schedule(ceil(delay).toLong()) {
            action()
        }

        nextAvailableTime += 100.milliseconds
    }

    fun chat(message: String) {
        val player = mc.thePlayer ?: return

        schedule {
            EventBus.messages.add(message)
            player.sendChatMessage(message)
            if (Debug.debugmode) addMessage("$prefix §fSent message \"$message\"")
        }
    }

    fun clientCommand(command: String) {
        val cmd = if (command.startsWith("/")) command else "/$command"
        ClientCommandHandler.instance.executeCommand(mc.thePlayer, cmd)
        if (Debug.debugmode) addMessage("$prefix §fSent client-side command \"$cmd\"")
    }

    fun command(command: String) {
        val player = mc.thePlayer ?: return
        val cmd = if (command.startsWith("/")) command else "/$command"

        schedule {
            EventBus.messages.add(cmd)
            player.sendChatMessage(cmd)
            if (Debug.debugmode) addMessage("$prefix §fSent command \"$cmd\"")
        }
    }

    @JvmOverloads
    fun addMessage(message: String, hover: String? = null, clickAction: ClickEvent.Action? = null, clickValue: String? = null, siblingText: String? = null) {
        val player = mc.thePlayer ?: return
        val component = ChatComponentText(message)
        siblingText?.let { text ->
            val sibling = ChatComponentText(text).apply {
                chatStyle = createChatStyle(hover, clickAction, clickValue)
            }
            component.appendSibling(sibling)
        } ?: run {
            component.chatStyle = createChatStyle(hover, clickAction, clickValue)
        }
        player.addChatMessage(component)
    }

    fun createChatStyle(hover: String?, clickAction: ClickEvent.Action?, clickValue: String?) =
        ChatStyle().apply {
            hover?.let { chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(it)) }
            if (clickAction != null && clickValue != null) chatClickEvent = ClickEvent(clickAction, clickValue)
        }

    private data class Threshold(val value: Double, val symbol: String, val precision: Int)
    private val thresholds = listOf(Threshold(1e9, "b", 1), Threshold(1e6, "m", 1), Threshold(1e3, "k", 1))

    fun formatNumber(number: String): String {
        return try {
            val num = number.replace(",", "").toDouble()
            val threshold = thresholds.find { num >= it.value }

            if (threshold != null) {
                val formatted = num / threshold.value
                val rounded = String.format("%.${threshold.precision}f", formatted).toDouble()
                "${rounded}${threshold.symbol}"
            } else {
                if (num == num.toLong().toDouble()) num.toLong().toString()
                else num.toString()
            }
        } catch (_: NumberFormatException) {
            number
        }
    }
}