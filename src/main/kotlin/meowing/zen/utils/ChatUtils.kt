package meowing.zen.utils

import net.minecraft.client.Minecraft
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

    fun addMessage(message: String) {
        val player = Minecraft.getMinecraft().thePlayer ?: return
        player.addChatMessage(ChatComponentText(message))
    }

    fun addMessage(message: String, hover: String) {
        val player = Minecraft.getMinecraft().thePlayer ?: return
        val component = ChatComponentText(message)
        val hoverText = ChatComponentText(hover)
        component.chatStyle = ChatStyle().apply {
            chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)
        }
        player.addChatMessage(component)
    }
}