package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object slayertimer {
    private val fail = Pattern.compile("^ {2}SLAYER QUEST FAILED!$")
    private val questStart = Pattern.compile("^ {2}SLAYER QUEST STARTED!$")

    @JvmField var BossId = -1
    @JvmField var isFighting = false
    private var startTime = 0L
    private var spawnTime = 0L
    private var serverTicks = 0

    @JvmStatic
    fun initialize() {
        Zen.registerListener("slayertimer", this)
    }

    fun handleBossSpawn(entityId: Int) {
        if (isFighting) return
        BossId = entityId - 3
        startTime = System.currentTimeMillis()
        isFighting = true
        serverTicks = 0
        registerEvents()
        resetSpawnTimer()
    }

    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return
        val text = event.message.unformattedText.removeFormatting()
        when {
            fail.matcher(text).matches() -> onSlayerFailed()
            questStart.matcher(text).matches() -> spawnTime = System.currentTimeMillis()
        }
    }

    @SubscribeEvent
    fun onEntityDeath(event: net.minecraftforge.event.entity.living.LivingDeathEvent) {
        val entity = event.entity
        if (entity !is net.minecraft.entity.EntityLivingBase || entity.entityId != BossId || !isFighting) return

        val timeTaken = System.currentTimeMillis() - startTime
        sendTimerMessage("You killed your boss", timeTaken, serverTicks)
        resetBossTracker()
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
        unregisterEvents()
    }

    private fun resetSpawnTimer() {
        if (spawnTime == 0L) return
        val spawnSeconds = (System.currentTimeMillis() - spawnTime) / 1000.0
        val content = "§c[Zen] §fYour boss spawned in §b${"%.2f".format(spawnSeconds)}s"
        ChatUtils.addMessage(content)
        spawnTime = 0
    }

    private val tickCounter = object {
        @SubscribeEvent
        fun onServerTick(event: meowing.zen.events.ServerTickEvent) {
            serverTicks++
        }
    }

    private fun registerEvents() {
        try {
            MinecraftForge.EVENT_BUS.register(tickCounter)
            if (Zen.config.slayerhighlight) MinecraftForge.EVENT_BUS.register(slayerhighlight)
        } catch (e: Exception) {
            println("[Zen] Failed to register event: ${e.message}")
        }
    }

    private fun unregisterEvents() {
        try {
            MinecraftForge.EVENT_BUS.unregister(tickCounter)
            if (Zen.config.slayerhighlight) MinecraftForge.EVENT_BUS.unregister(slayerhighlight)
        } catch (e: Exception) {
            println("[Zen] Failed to unregister event: ${e.message}")
        }
    }
}