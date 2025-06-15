package meowing.zen.utils

import net.minecraft.client.Minecraft

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
        player.addChatMessage(net.minecraft.util.ChatComponentText(message))
    }

    fun removeFormatting(text: String): String {
        return text.replace(Regex("[§&][0-9a-f-r]", RegexOption.IGNORE_CASE), "")
    }
}