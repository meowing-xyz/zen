package meowing.zen.api

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.events.*
import meowing.zen.features.slayers.SlayerTimer
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.monster.EntitySpider
import kotlin.time.Duration

@Zen.Module
object SlayerTracker {
    private val slayertimer by ConfigDelegate<Boolean>("slayertimer")

    private val slayerType = " {3}(?<type>.*) Slayer LVL .* - Next LVL in .*".toRegex()

    var spawnTime = TimeUtils.zero
    var isFighting = false
    var startTime = TimeUtils.zero
    var bossType = ""

    private var serverTicks = 0
    private var isSpider = false
    private var serverTickCall: EventBus.EventCall = EventBus.register<TickEvent.Server> ({ serverTicks++ }, false)

    var sessionKills = 0
    var sessionStart = TimeUtils.now
    var totalKillTime = Duration.ZERO
    var totalSpawnTime = Duration.ZERO

    init {
        EventBus.register<SkyblockEvent.Slayer.QuestStart> {
            spawnTime = TimeUtils.now
        }
        EventBus.register<ChatEvent.Receive> {
            val message = it.event.message.unformattedText.removeFormatting()

            slayerType.find(message)?.let { match ->
                bossType = match.groups["type"]?.value ?: ""
            }
        }

        EventBus.register<SkyblockEvent.Slayer.Spawn> { _ ->
            if (!isFighting && !isSpider) {
                startTime = TimeUtils.now
                isFighting = true
                serverTicks = 0
                serverTickCall.register()

                if(slayertimer) SlayerTimer.sendBossSpawnMessage(spawnTime)

                totalSpawnTime += spawnTime.since
                spawnTime = TimeUtils.zero
            }
        }

        EventBus.register<WorldEvent.Change> {
            resetBossTracker()
            spawnTime = TimeUtils.now
        }

        EventBus.register<SkyblockEvent.Slayer.Death> { event ->
            if (isFighting) {
                if (event.entity is EntitySpider && !isSpider) {
                    isSpider = true
                    return@register
                }
                val timeTaken = startTime.since

                sessionKills ++
                totalKillTime += timeTaken

                if(slayertimer) SlayerTimer.sendTimerMessage("You killed your boss", timeTaken.millis, serverTicks)

                resetBossTracker()
            }
        }

        EventBus.register<SkyblockEvent.Slayer.Fail> {
            if (!isFighting) return@register
            val timeTaken = startTime.since.millis

            if(slayertimer) SlayerTimer.sendTimerMessage("Your boss killed you", timeTaken, serverTicks)

            resetBossTracker()
        }

        EventBus.register<SkyblockEvent.Slayer.Cleanup> {
            resetBossTracker()
        }
    }

    private fun resetBossTracker() {
        startTime = TimeUtils.zero
        isFighting = false
        isSpider = false
        serverTicks = 0
        serverTickCall.unregister()
    }

    fun reset() {
        sessionKills = 0
        sessionStart = TimeUtils.now
        totalKillTime = Duration.ZERO
        totalSpawnTime = Duration.ZERO
    }
}