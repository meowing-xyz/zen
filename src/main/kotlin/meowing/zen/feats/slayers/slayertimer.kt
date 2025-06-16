package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.utils.ChatUtils
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.regex.Pattern

class slayertimer private constructor() {
    companion object {
        private val instance = slayertimer()
        private val mc = Minecraft.getMinecraft()
        private val fail = Pattern.compile("^ {2}SLAYER QUEST FAILED!$")
        private val questStart = Pattern.compile("^ {2}SLAYER QUEST STARTED!$")

        @JvmField
        var BossId = -1

        @JvmField
        var isFighting = false

        private var starttime = 0L
        private var spawntime = 0L
        private var serverticks = 0

        @JvmStatic
        fun initialize() {
            Zen.registerListener("slayertimer", instance)
        }

        @JvmStatic
        fun onEntityMetadataUpdate(packet: S1CPacketEntityMetadata) {
            packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
                val name = ChatUtils.removeFormatting(obj.`object` as String)
                if (name.contains("Spawned by") && name.endsWith("by: ${mc.thePlayer?.name}")) {
                    BossId = packet.entityId - 3
                    starttime = System.currentTimeMillis()
                    isFighting = true
                    serverticks = 0
                    resetSpawnTimer()
                }
            }
        }

        private fun onSlayerFailed() {
            if (!isFighting) return
            val timetaken = System.currentTimeMillis() - starttime
            sendTimerMessage("Your boss killed you", timetaken, serverticks)
            resetBossTracker()
        }

        private fun sendTimerMessage(action: String, timetaken: Long, ticks: Int) {
            val seconds = timetaken / 1000.0
            val servertime = ticks / 20.0
            val content = "§c[Zen] §f$action in §b%.2fs §7| §b%.2fs".format(seconds, servertime)
            val hovercontent = "§c%d ms §f| §c%.0f ticks".format(timetaken, ticks.toFloat())

            val message = ChatComponentText(content)
            val hoverText = ChatComponentText(hovercontent)
            val style = ChatStyle()
            style.chatHoverEvent = net.minecraft.event.HoverEvent(net.minecraft.event.HoverEvent.Action.SHOW_TEXT, hoverText)
            message.chatStyle = style

            mc.thePlayer?.addChatMessage(message)
        }

        private fun resetBossTracker() {
            BossId = -1
            starttime = 0
            isFighting = false
            serverticks = 0
        }

        private fun resetSpawnTimer() {
            if (spawntime == 0L) return
            val spawnsecond = (System.currentTimeMillis() - spawntime) / 1000.0
            val content = "§c[Zen] §fYour boss spawned in §b%.2fs".format(spawnsecond)
            mc.thePlayer?.addChatMessage(ChatComponentText(content))
            spawntime = 0
        }
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        if (isFighting) serverticks++
    }

    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return
        val text = ChatUtils.removeFormatting(event.message.unformattedText)
        if (fail.matcher(text).matches()) onSlayerFailed()
        if (questStart.matcher(text).matches()) spawntime = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        if (event.entity !is EntityLivingBase || event.entity.entityId != BossId) return
        val timetaken = System.currentTimeMillis() - starttime
        sendTimerMessage("You killed your boss", timetaken, serverticks)
        resetBossTracker()
    }
}