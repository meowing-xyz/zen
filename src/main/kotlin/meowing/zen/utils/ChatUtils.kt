package meowing.zen.utils

import net.minecraft.client.Minecraft
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle

object ChatUtils {
    fun chat(message: String) {
        val player = Minecraft.getMinecraft().thePlayer ?: return
        player.sendChatMessage(message)
    }

    fun command(command: String) {
        val player = Minecraft.getMinecraft().thePlayer ?: return
        val cmd = if (command.startsWith("/")) command else "/$command"
        player.sendChatMessage(cmd)
    }

    fun addMessage(message: String, hover: String? = null, clickAction: ClickEvent.Action? = null, clickValue: String? = null, siblingText: String? = null) {
        val player = Minecraft.getMinecraft().thePlayer ?: return
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
        } catch (e: NumberFormatException) {
            number
        }
    }
}