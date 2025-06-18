package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.utils.ChatUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatStyle
import tv.twitch.chat.Chat

object partyfinder {
    private val playerName get() = Minecraft.getMinecraft().thePlayer?.name ?: ""
    private val joinedPattern = Regex("^Party Finder > (.+?) joined the dungeon group! \\((\\w+) Level (\\d+)\\)$")
    private val classSetPattern = Regex("^Party Finder > (.+?) set their class to (\\w+) Level (\\d+)!$")

    @JvmStatic
    fun initialize() {
        Zen.registerListener("partyfindermsgs", this)
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val text = event.message.unformattedText

        when {
            text == "Party Finder > Your party has been queued in the dungeon finder!" -> {
                event.isCanceled = true
                ChatUtils.addMessage("§c§lPF §7> §fParty queued.")
            }

            text == "Party Finder > Your group has been de-listed!" -> {
                event.isCanceled = true
                ChatUtils.addMessage("§c§lPF §7> §fParty delisted.")
            }

            joinedPattern.matches(text) -> {
                event.isCanceled = true
                val (user, cls, lvl) = joinedPattern.find(text)!!.destructured

                if (user == playerName) ChatUtils.addMessage("§c§lPF §7> §b$user §8| §b$cls §7- §b$lvl")
                else {
                    val player = Minecraft.getMinecraft().thePlayer ?: return
                    val base = ChatComponentText("§c§lPF §7> §b$user §8| §b$cls §7- §b$lvl")
                    base.appendSibling(ChatComponentText(" §8| "))
                    base.appendSibling(
                        ChatComponentText("§a[✖]").apply {
                            chatStyle = ChatStyle().apply {
                                chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/p kick $user")
                                chatHoverEvent = HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    ChatComponentText("§cKick §b$user")
                                )
                            }
                        }
                    )
                    base.appendSibling(ChatComponentText(" §8| "))
                    base.appendSibling(
                        ChatComponentText("§a[PV]").apply {
                            chatStyle = ChatStyle().apply {
                                chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pv $user")
                                chatHoverEvent = HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    ChatComponentText("§cPV §b$user")
                                )
                            }
                        }
                    )

                    player.addChatMessage(base)
                }
            }

            classSetPattern.matches(text) -> {
                event.isCanceled = true
                val (user, cls, lvl) = classSetPattern.find(text)!!.destructured
                ChatUtils.addMessage("§c§lPF §7> §b$user §fchanged to §b$cls §7- §b$lvl")
            }
        }
    }
}
