package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.events.ServerTickEvent
import meowing.zen.events.EntityMetadataUpdateEvent
import meowing.zen.utils.ChatUtils.addMessage
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object slayertimer {
    private val mc = Minecraft.getMinecraft()
    private val fail = Pattern.compile("^ {2}SLAYER QUEST FAILED!$")
    private val questStart = Pattern.compile("^ {2}SLAYER QUEST STARTED!$")

    @JvmField var BossId = -1
    @JvmField var isFighting = false
    private var starttime = 0L
    private var spawntime = 0L
    private var serverticks = 0

    @JvmStatic
    fun initialize() {
        Zen.registerListener("slayertimer", this)
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        if (isFighting) serverticks++
    }

    @SubscribeEvent
    fun onEntityMetadataUpdate(event: EntityMetadataUpdateEvent) {
        event.packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
            val name = (obj.`object` as String).removeFormatting()
            if (name.contains("Spawned by") && name.endsWith("by: ${mc.thePlayer?.name}") && !isFighting) {
                BossId = event.packet.entityId - 3
                starttime = System.currentTimeMillis()
                isFighting = true
                serverticks = 0
                resetSpawnTimer()
            }
        }
    }

    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return
        val text = event.message.unformattedText.removeFormatting()
        when {
            fail.matcher(text).matches() -> onSlayerFailed()
            questStart.matcher(text).matches() -> spawntime = System.currentTimeMillis()
        }
    }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        if (event.entity !is EntityLivingBase || event.entity.entityId != BossId) return
        val timetaken = System.currentTimeMillis() - starttime
        val ticks = serverticks
        sendTimerMessage("You killed your boss", timetaken, ticks)
        resetBossTracker()
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
        val hoverText = "§c%d ms §f| §c%.0f ticks".format(timetaken, ticks.toFloat())
        addMessage(content, hoverText)
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
        addMessage(content)
        spawntime = 0
    }
}