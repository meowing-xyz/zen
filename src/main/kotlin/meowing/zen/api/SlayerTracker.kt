package meowing.zen.api

import meowing.zen.Zen
import meowing.zen.api.EntityDetection.bossID
import meowing.zen.config.ConfigDelegate
import meowing.zen.events.*
import meowing.zen.features.slayers.SlayerTimer
import meowing.zen.utils.TickUtils
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntitySpider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

// TODO: reset and hide the slayer stats after some time of not being in a fight or swapping worlds
@Zen.Module
object SlayerTracker {
    private val slayertimer by ConfigDelegate<Boolean>("slayertimer")
    private val slayerMobRegex = "(?<=☠\\s)[A-Za-z]+\\s[A-Za-z]+(?:\\s[IVX]+)?".toRegex()
    // Maybe add Slayer XP / hour later https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/blob/63fd91727910a671d64f54e21a74f7624b2aecde/constants/leveling.json#L170

    var slayerSpawnedAtTime = TimeUtils.zero
        private set
    var pauseStart: Long? = null
    var isPaused = false
    var totalPaused: Long = 0
    var bossType = ""
        private set
    var isFightingBoss = false
        private set
    private var isSpider = false
    private var serverTicks = 0
    private var serverTickCall: EventBus.EventCall =
        EventBus.register<TickEvent.Server>({ serverTicks++ }, false)

    var sessionBossKills = 0
        private set
    var sessionStart = TimeUtils.zero
        private set
    var totalKillTime = Duration.ZERO
        private set
    var totalSpawnTime = Duration.ZERO
        private set

    var questStartTime = TimeUtils.zero

    var killRegex = " (?<kills>.*)/(?<target>.*) Kills".toRegex()
    var currentMobKills = 0
    var mobLastKilledAt = TimeUtils.zero

    private fun startFightTimer() {
        slayerSpawnedAtTime = TimeUtils.now
        pauseStart = null
        isPaused = false
    }

    private fun pauseSessionTimer() {
        if (pauseStart == null) {
            pauseStart = System.currentTimeMillis()
            isPaused = true
        }
    }

    private fun resumeSessionTimer() {
        if(!isPaused) return

        pauseStart?.let {
            totalPaused += System.currentTimeMillis() - it
            pauseStart = null
            isPaused = false
        }
    }

    init {
        EventBus.register<TickEvent.Server> {
            if (pauseStart == null &&
                mobLastKilledAt != TimeUtils.zero &&
                mobLastKilledAt.since.inWholeSeconds >= 15 &&
                !isFightingBoss
            ) {
                pauseSessionTimer()
            }
        }

        EventBus.register<SkyblockEvent.Slayer.QuestStart> {
            questStartTime = TimeUtils.now
        }

        EventBus.register<SidebarUpdateEvent> { event ->
            event.lines.firstNotNullOfOrNull { killRegex.find(it) }?.let { match ->
                val killsInt = match.groupValues[1].toIntOrNull() ?: return@register

                if (killsInt != currentMobKills) {
                    // Start the session timer if it's not already started
                    if (sessionStart.isZero) sessionStart = TimeUtils.now
                    if (questStartTime.isZero) questStartTime = TimeUtils.now

                    mobLastKilledAt = TimeUtils.now
                    currentMobKills = killsInt

                    if (isPaused) {
                        resumeSessionTimer()
                    }
                }
            }
        }

        EventBus.register<SkyblockEvent.Slayer.Spawn> { _ ->
            if (!isFightingBoss && !isSpider) {
                isFightingBoss = true
                serverTicks = 0
                serverTickCall.register()

                val rawElapsed: Duration = questStartTime.since
                val adjustedTime: Duration = rawElapsed - totalPaused.milliseconds
                if (slayertimer) SlayerTimer.sendBossSpawnMessage(adjustedTime)

                totalSpawnTime += adjustedTime
                questStartTime = TimeUtils.zero
                currentMobKills = 0

                resumeSessionTimer()
                startFightTimer()
            }
        }

        EventBus.register<EntityEvent.Join> { event ->
            TickUtils.scheduleServer(2) {
                if (bossID != null && event.entity.entityId == bossID!! + 1 && event.entity is EntityArmorStand) {
                    val name = event.entity.name.removeFormatting()
                    slayerMobRegex.find(name)?.let { matchResult ->
                        bossType = matchResult.value
                    }
                }
            }
        }

        EventBus.register<SkyblockEvent.Slayer.Death> { event ->
            if (!isFightingBoss) return@register
            if (event.entity is EntitySpider && !isSpider) {
                isSpider = true
                return@register
            }

            val timeToKill = slayerSpawnedAtTime.since
            sessionBossKills++
            totalKillTime += timeToKill

            if (slayertimer) {
                SlayerTimer.sendTimerMessage(
                    "You killed your boss",
                    timeToKill,
                    serverTicks
                )
            }

            resetBossTracker()
        }

        EventBus.register<WorldEvent.Change> {
            mobLastKilledAt = TimeUtils.zero
        }

        EventBus.register<SkyblockEvent.Slayer.Fail> {
            if (!isFightingBoss) return@register

            if (slayertimer) {
                SlayerTimer.sendTimerMessage(
                    "Your boss killed you",
                    slayerSpawnedAtTime.since,
                    serverTicks
                )
            }

            resetBossTracker()
        }

        EventBus.register<SkyblockEvent.Slayer.Cleanup> {
            resetBossTracker()
        }
    }

    private fun resetBossTracker() {
        slayerSpawnedAtTime = TimeUtils.zero
        pauseStart = null
        isFightingBoss = false
        isPaused = false
        isSpider = false
        serverTicks = 0
        bossType = ""
        serverTickCall.unregister()
    }

    fun reset() {
        sessionBossKills = 0
        sessionStart = TimeUtils.now
        totalKillTime = Duration.ZERO
        totalSpawnTime = Duration.ZERO
        totalPaused = 0
    }
}
