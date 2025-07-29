package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.events.TickEvent
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.feats.Feature
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntitySpider

@Zen.Module
object SlayerTimer : Feature("slayertimer") {
    @JvmField var BossId = -1
    @JvmField var isFighting = false
    var spawnTime = TimeUtils.zero
    private val fail = "^ {2}SLAYER QUEST FAILED!$".toRegex()
    private val questStart = "^ {2}SLAYER QUEST STARTED!$".toRegex()
    private var startTime = TimeUtils.zero
    private var serverTicks = 0
    private var isSpider = false
    private var serverTickCall: EventBus.EventCall = EventBus.register<TickEvent.Server> ({ serverTicks++ }, false)
    private val slayerstats by ConfigDelegate<Boolean>("slayerstats")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "General", ConfigElement(
                "slayertimer",
                "Slayer timer",
                "Sends a message in your chat telling you how long it took to kill your boss.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            val text = event.event.message.unformattedText.removeFormatting()
            when {
                fail.matches(text) -> onSlayerFailed()
                questStart.matches(text) -> spawnTime = TimeUtils.now
            }
        }

        register<EntityEvent.Leave> { event ->
            if (event.entity is EntityLivingBase && event.entity.entityId == BossId && isFighting) {
                if (event.entity is EntitySpider && !isSpider) {
                    isSpider = true
                    return@register
                }
                val timeTaken = startTime.since
                sendTimerMessage("You killed your boss", timeTaken.millis, serverTicks)
                if (slayerstats) SlayerStats.addKill(timeTaken)
                resetBossTracker()
            }
        }
    }

    fun handleBossSpawn(entityId: Int) {
        if (!isFighting) {
            BossId = entityId - 3
            if (!isSpider) {
                startTime = TimeUtils.now
                isFighting = true
                serverTicks = 0
                serverTickCall.register()
                resetSpawnTimer()
            }
        }
    }

    private fun onSlayerFailed() {
        if (!isFighting) return
        val timeTaken = startTime.since.millis
        sendTimerMessage("Your boss killed you", timeTaken, serverTicks)
        resetBossTracker()
    }

    private fun sendTimerMessage(action: String, timeTaken: Long, ticks: Int) {
        val seconds = timeTaken / 1000.0
        val serverTime = ticks / 20.0
        val content = "$prefix §f$action in §b${"%.2f".format(seconds)}s §7| §b${"%.2f".format(serverTime)}s"
        val hoverText = "§c${timeTaken}ms §f| §c${"%.0f".format(ticks.toFloat())} ticks"
        ChatUtils.addMessage(content, hoverText)
    }

    private fun resetBossTracker() {
        BossId = -1
        startTime = TimeUtils.zero
        isFighting = false
        isSpider = false
        serverTicks = 0
        serverTickCall.unregister()
    }

    private fun resetSpawnTimer() {
        if (spawnTime.isZero) return
        val spawnSeconds = spawnTime.since.millis / 1000.0
        val content = "$prefix §fYour boss spawned in §b${"%.2f".format(spawnSeconds)}s"
        ChatUtils.addMessage(content)
        spawnTime = TimeUtils.zero
    }
}