package meowing.zen.api

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mayorData
import meowing.zen.api.EntityDetection.bossID
import meowing.zen.config.ConfigDelegate
import meowing.zen.events.*
import meowing.zen.features.slayers.SlayerTimer
import meowing.zen.utils.SimpleTimeMark
import meowing.zen.utils.TickUtils
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntitySpider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Zen.Module
object SlayerTracker {
    private val slayertimer by ConfigDelegate<Boolean>("slayertimer")
    private val slayerMobRegex = "(?<=☠\\s)[A-Za-z]+\\s[A-Za-z]+(?:\\s[IVX]+)?".toRegex()
    private val killRegex = " (?<kills>.*)/(?<target>.*) Kills".toRegex()
    private val tierXp = mapOf("I" to 5, "II" to 25, "III" to 100, "IV" to 500, "V" to 1500)

    private val serverTickCall = EventBus.register<TickEvent.Server>({ serverTicks++ }, false)

    private var slayerSpawnedAtTime = TimeUtils.zero
    private var currentMobKills = 0
    private var isFightingBoss = false
    private var isSpider = false
    private var serverTicks = 0
    private var totalCurrentPaused: Long = 0

    var isPaused = false
    var pauseStart: SimpleTimeMark? = null
    var totalSessionPaused: Long = 0

    var sessionBossKills = 0
    var sessionStart = TimeUtils.zero
    var totalKillTime = Duration.ZERO
    var totalSpawnTime = Duration.ZERO
    var questStartedAtTime = TimeUtils.zero
    var mobLastKilledAt = TimeUtils.zero

    var bossType = ""
    var xpPerKill = 0

    private fun startFightTimer() {
        slayerSpawnedAtTime = TimeUtils.now
        pauseStart = null
        isPaused = false
        totalCurrentPaused = 0
    }

    private fun pauseSessionTimer() {
        if (pauseStart == null) {
            pauseStart = TimeUtils.now
            isPaused = true
        }
    }

    private fun resumeSessionTimer() {
        if(!isPaused || pauseStart == null) return

        totalSessionPaused += pauseStart!!.since.millis
        totalCurrentPaused += pauseStart!!.since.millis
        pauseStart = null
        isPaused = false
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
            questStartedAtTime = TimeUtils.now
        }

        EventBus.register<SidebarUpdateEvent> { event ->
            event.lines.firstNotNullOfOrNull { killRegex.find(it) }?.let { match ->
                val killsInt = match.groupValues[1].toIntOrNull() ?: return@register

                if (killsInt != currentMobKills) {
                    // Start the session timer if it's not already started
                    if (sessionStart.isZero) sessionStart = TimeUtils.now
                    if (questStartedAtTime.isZero) questStartedAtTime = TimeUtils.now

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

                if (!questStartedAtTime.isZero) {
                    val adjustedTime = (questStartedAtTime.since - totalCurrentPaused.milliseconds)
                    if (slayertimer) SlayerTimer.sendBossSpawnMessage(adjustedTime)

                    totalSpawnTime += adjustedTime
                }

                questStartedAtTime = TimeUtils.zero
                mobLastKilledAt = TimeUtils.now
                totalCurrentPaused = 0

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
                        xpPerKill = getBossXP(bossType)
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
        totalCurrentPaused = 0
    }

    private fun getBossXP(bossName: String): Int {
        val xp = when {
            bossName.endsWith(" V") -> tierXp["V"]!!
            bossName.endsWith(" IV") -> tierXp["IV"]!!
            bossName.endsWith(" III") -> tierXp["III"]!!
            bossName.endsWith(" II") -> tierXp["II"]!!
            bossName.endsWith(" I") -> tierXp["I"]!!
            else -> 0
        }

        val isAatrox = mayorData?.mayor?.perks?.any { it.name == "Slayer XP Buff" } == true || mayorData?.mayor?.minister?.perks?.any { it.name == "Slayer XP Buff" } == true
        if (isAatrox) return (xp * 1.25).toInt()

        return xp
    }

    fun reset() {
        sessionBossKills = 0
        sessionStart = TimeUtils.now
        totalKillTime = Duration.ZERO
        totalSpawnTime = Duration.ZERO
        totalSessionPaused = 0
    }
}