package meowing.zen.feats.dungeons

import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.events.ChatReceiveEvent
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatStyle

object partyfinder : Feature("partyfindermsgs") {
    private val playerName get() = Minecraft.getMinecraft().thePlayer?.name ?: ""
    private val joinedPattern = Regex("^Party Finder > (.+?) joined the dungeon group! \\((\\w+) Level (\\d+)\\)$")
    private val classSetPattern = Regex("^Party Finder > (.+?) set their class to (\\w+) Level (\\d+)!$")

    override fun initialize() {
        register<ChatReceiveEvent> { event ->
            val text = event.event.message.unformattedText

            when {
                text == "Party Finder > Your party has been queued in the dungeon finder!" -> {
                    event.cancel()
                    ChatUtils.addMessage("§c§lParty finder §7> §fParty queued.")
                }

                text == "Party Finder > Your group has been de-listed!" -> {
                    event.cancel()
                    ChatUtils.addMessage("§c§lParty finder §7> §fParty delisted.")
                }

                joinedPattern.matches(text) -> {
                    event.cancel()
                    val (user, cls, lvl) = joinedPattern.find(text)!!.destructured

                    if (user == playerName) ChatUtils.addMessage("§c§lParty finder §7> §b$user §8| §b$cls §7- §b$lvl")
                    else {
                        val player = Minecraft.getMinecraft().thePlayer ?: return@register
                        val base = ChatComponentText("§c§lParty finder §7> §b$user §8| §b$cls §7- §b$lvl")
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
                    event.cancel()
                    val (user, cls, lvl) = classSetPattern.find(text)!!.destructured
                    ChatUtils.addMessage("§c§lParty finder §7> §b$user §fchanged to §b$cls §7- §b$lvl")
                }
            }
        }
    }
}