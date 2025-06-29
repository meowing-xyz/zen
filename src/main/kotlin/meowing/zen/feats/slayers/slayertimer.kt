package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.events.ChatMessageEvent
import meowing.zen.events.EntityLeaveEvent
import meowing.zen.events.ServerTickEvent
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern
import meowing.zen.feats.Feature
import net.minecraft.entity.EntityLivingBase

object slayertimer : Feature("slayertimer") {
    @JvmField var BossId = -1
    @JvmField var isFighting = false

    private val fail = Pattern.compile("^ {2}SLAYER QUEST FAILED!$")
    private val questStart = Pattern.compile("^ {2}SLAYER QUEST STARTED!$")
    private var startTime = 0L
    private var spawnTime = 0L
    private var serverTicks = 0

    override fun initialize() {
        register<ChatMessageEvent> { event ->
            val text = event.message.removeFormatting()
            when {
                fail.matcher(text).matches() -> onSlayerFailed()
                questStart.matcher(text).matches() -> spawnTime = System.currentTimeMillis()
            }
        }

        register<EntityLeaveEvent> { event ->
            if (event.entity is EntityLivingBase && event.entity.entityId == BossId && isFighting) {
                val timeTaken = System.currentTimeMillis() - startTime
                sendTimerMessage("You killed your boss", timeTaken, serverTicks)
                if (Zen.config.slayerstats) slayerstats.addKill(timeTaken)
                resetBossTracker()
            }
        }

        register<ServerTickEvent> {
            serverTicks++
        }
    }

    fun handleBossSpawn(entityId: Int) {
        if (isFighting) return
        BossId = entityId - 3
        startTime = System.currentTimeMillis()
        isFighting = true
        serverTicks = 0
        resetSpawnTimer()
    }

    private fun onSlayerFailed() {
        if (!isFighting) return
        val timeTaken = System.currentTimeMillis() - startTime
        sendTimerMessage("Your boss killed you", timeTaken, serverTicks)
        resetBossTracker()
    }

    private fun sendTimerMessage(action: String, timeTaken: Long, ticks: Int) {
        val seconds = timeTaken / 1000.0
        val serverTime = ticks / 20.0
        val content = "§c[Zen] §f$action in §b${"%.2f".format(seconds)}s §7| §b${"%.2f".format(serverTime)}s"
        val hoverText = "§c${timeTaken}ms §f| §c${"%.0f".format(ticks.toFloat())} ticks"
        ChatUtils.addMessage(content, hoverText)
    }

    private fun resetBossTracker() {
        BossId = -1
        startTime = 0
        isFighting = false
        serverTicks = 0
    }

    private fun resetSpawnTimer() {
        if (spawnTime == 0L) return
        val spawnSeconds = (System.currentTimeMillis() - spawnTime) / 1000.0
        val content = "§c[Zen] §fYour boss spawned in §b${"%.2f".format(spawnSeconds)}s"
        ChatUtils.addMessage(content)
        spawnTime = 0
    }
}