package meowing.zen.features.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EventBus
import meowing.zen.events.SkyblockEvent
import meowing.zen.events.TickEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import net.minecraft.entity.monster.EntitySpider

@Zen.Module
object SlayerTimer : Feature("slayertimer", true) {
    var spawnTime = TimeUtils.zero
    private var isFighting = false
    private var startTime = TimeUtils.zero
    private var serverTicks = 0
    private var isSpider = false
    private var serverTickCall: EventBus.EventCall = EventBus.register<TickEvent.Server> ({ serverTicks++ }, false)
    private val slayerstats by ConfigDelegate<Boolean>("slayerstats")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Slayer timer", ConfigElement(
                "slayertimer",
                "Slayer timer",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Slayers", "Slayer timer", "", ConfigElement(
                "",
                null,
                ElementType.TextParagraph("Logs your time to kill slayer bosses to chat.")
            ))
    }

    override fun initialize() {
        register<SkyblockEvent.Slayer.QuestStart> {
            spawnTime = TimeUtils.now
        }

        register<SkyblockEvent.Slayer.Spawn> { _ ->
            if (!isFighting && !isSpider) {
                startTime = TimeUtils.now
                isFighting = true
                serverTicks = 0
                serverTickCall.register()
                resetSpawnTimer()
            }
        }

        register<SkyblockEvent.Slayer.Death> { event ->
            if (isFighting) {
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

        register<SkyblockEvent.Slayer.Fail> {
            if (!isFighting) return@register
            val timeTaken = startTime.since.millis
            sendTimerMessage("Your boss killed you", timeTaken, serverTicks)
            resetBossTracker()
        }

        register<SkyblockEvent.Slayer.Cleanup> {
            resetBossTracker()
        }
    }

    private fun sendTimerMessage(action: String, timeTaken: Long, ticks: Int) {
        val seconds = timeTaken / 1000.0
        val serverTime = ticks / 20.0
        val content = "$prefix §f$action in §b${"%.2f".format(seconds)}s §7| §b${"%.2f".format(serverTime)}s"
        val hoverText = "§c${timeTaken}ms §f| §c${"%.0f".format(ticks.toFloat())} ticks"
        ChatUtils.addMessage(content, hoverText)
    }

    private fun resetBossTracker() {
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