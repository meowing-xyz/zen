package meowing.zen.features.slayers.slayerdisplay

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.TickEvent
import meowing.zen.events.configRegister
import meowing.zen.features.Feature
import meowing.zen.features.carrying.CarryCounter
import meowing.zen.utils.Render3D
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.baseMaxHealth
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3

// TODO: Fix for blaze
@Zen.Module
object SlayerDisplay : Feature("slayerdisplay") {
    private var entities = mutableListOf<Int>()
    private var displayData = mutableMapOf<Int, String>()
    private var entitySpawnTimes = mutableMapOf<Int, Long>()
    private var killTimers = mutableMapOf<Int, Triple<Long, String, Vec3>>()
    private val slayerMobRegex = "(?<=☠\\s)[A-Za-z]+\\s[A-Za-z]+(?:\\s[IVX]+)?".toRegex()
    private val hpRegex = "(\\d+(?:,\\d{3})*(?:\\.\\d+)?[kmb]?)❤".toRegex(RegexOption.IGNORE_CASE)
    private val hitsRegex = "(\\d+)\\s+Hits".toRegex()
    private val timeRegex = "(\\d+):(\\d+)".toRegex()

    private val useFullName by ConfigDelegate<Boolean>("slayerdisplayusefullname")
    private val displayOptions by ConfigDelegate<Set<Int>>("slayerdisplayoptions")
    private val shownBossesOptions by ConfigDelegate<Set<Int>>("slayerdisplaybossshown")
    private val showKillTimer by ConfigDelegate<Boolean>("slayerdisplayshowkilltimer")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Slayer Display", ConfigElement(
                "slayerdisplay",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Slayers", "Slayer Display", "Options", ConfigElement(
                "slayerdisplayusefullname",
                "Use Full Mob Name",
                ElementType.Switch(false)
            ))
            .addElement("Slayers", "Slayer Display", "Options", ConfigElement(
                "slayerdisplayshowkilltimer",
                "Show Kill Timer",
                ElementType.Switch(true)
            ))
            .addElement("Slayers", "Slayer Display", "Options", ConfigElement(
                "slayerdisplayoptions",
                "Display Options",
                ElementType.MultiCheckbox(
                    listOf(
                        "Show Mob Name",
                        "Show Health",
                        "Show Hits",
                        "Show Phases",
                        "Show Laser Timer",
                        "Show Timer Nametag",
                        "Compact Display"
                    ),
                    setOf(0, 1, 2, 3, 4)
                )
            ))
            .addElement("Slayers", "Slayer Display", "Options", ConfigElement(
                "slayerdisplaybossshown",
                "Show Slayers For",
                ElementType.MultiCheckbox(
                    listOf(
                        "Show for all bosses",
                        "Only for carries",
                        "Only for mine",
                        "Mine and carries"
                    ),
                    setOf(0)
                )
            ))
    }

    override fun initialize() {
        register<EntityEvent.Metadata> { event ->
            val cleanName = event.name.removeFormatting().replace(",", "")
            slayerMobRegex.find(cleanName)?.let {
                val entityId = event.packet.entityId - 1
                entities.add(entityId)
                if (!entitySpawnTimes.containsKey(entityId)) {
                    entitySpawnTimes[entityId] = System.currentTimeMillis()
                }
            }
        }

        register<EntityEvent.Leave> { event ->
            val entityId = event.entity.entityId
            if (entities.contains(entityId) && showKillTimer) {
                entitySpawnTimes[entityId]?.let { spawnTime ->
                    val killTime = System.currentTimeMillis() - spawnTime
                    val seconds = killTime / 1000.0
                    val killTimeText = "§a${String.format("%.1f", seconds)}s"
                    val position = event.entity.positionVector
                    killTimers[entityId] = Triple(System.currentTimeMillis(), killTimeText, position)
                }
            }
            entities.remove(entityId)
            displayData.remove(entityId)
            entitySpawnTimes.remove(entityId)
        }

        register<TickEvent.Client> {
            displayData.clear()

            killTimers.entries.removeAll { (_, timerData) ->
                System.currentTimeMillis() - timerData.first > 3000
            }

            entities.removeAll { entityId ->
                val entity = world?.getEntityByID(entityId) ?: return@removeAll true
                val nametag = world?.getEntityByID(entityId + 1)?.name?.removeFormatting() ?: return@removeAll true
                val timerNametag = world?.getEntityByID(entityId + 2)?.name?.removeFormatting() ?: ""
                val spawnerNametag = world?.getEntityByID(entityId + 3)?.name?.removeFormatting() ?: ""

                val shouldShow = when {
                    0 in shownBossesOptions -> true
                    1 in shownBossesOptions -> {
                        spawnerNametag.contains("Spawned by") && (CarryCounter.carryees.any { carryee -> spawnerNametag.endsWith("by: ${carryee.name}") })
                    }
                    2 in shownBossesOptions -> {
                        spawnerNametag.contains("Spawned by") && spawnerNametag.endsWith("by: ${player?.name}")
                    }
                    3 in shownBossesOptions -> {
                        spawnerNametag.contains("Spawned by") && (spawnerNametag.endsWith("by: ${player?.name}") || CarryCounter.carryees.any { carryee -> spawnerNametag.endsWith("by: ${carryee.name}") })
                    }
                    else -> false
                }

                if (!shouldShow) return@removeAll false

                val bossType = getBossType(nametag) ?: return@removeAll false
                val hpMatch = hpRegex.find(nametag)
                val hitsMatch = hitsRegex.find(nametag)

                if ((hpMatch != null && 1 in displayOptions) || (hitsMatch != null && 2 in displayOptions)) {
                    val mobName = if (useFullName) bossType.fullName else bossType.shortName
                    val laserTimer = getLaserTimer(entity)
                    val useCompactDisplay = 6 in displayOptions

                    val displayLines = mutableListOf<String>()
                    val showName = 0 in displayOptions
                    val nameWithTimer = if (laserTimer.isNotEmpty()) "$mobName $laserTimer" else mobName

                    if (5 in displayOptions && timerNametag.isNotEmpty()) {
                        val formattedTimer = formatTimerNametag(timerNametag)
                        if (formattedTimer.isNotEmpty()) {
                            displayLines.add(formattedTimer)
                        }
                    }

                    when {
                        hpMatch != null && 1 in displayOptions -> {
                            val currentHp = parseHp(hpMatch.groupValues[1])
                            val maxHp = (entity as EntityLivingBase).baseMaxHealth
                            val hpColor = getHpColor(currentHp, maxHp)
                            val phaseText = getSlayerPhaseText(bossType, currentHp, maxHp)
                            val healthText = "$hpColor${hpMatch.groupValues[1]}"

                            when {
                                showName && useCompactDisplay -> displayLines.add("$phaseText$nameWithTimer $healthText")
                                showName -> {
                                    displayLines.add(nameWithTimer)
                                    displayLines.add("$phaseText$healthText")
                                }
                                laserTimer.isNotEmpty() && useCompactDisplay -> displayLines.add("$laserTimer $phaseText$healthText")
                                laserTimer.isNotEmpty() -> {
                                    displayLines.add(laserTimer)
                                    displayLines.add("$phaseText$healthText")
                                }
                                else -> displayLines.add("$phaseText$healthText")
                            }
                        }
                        hitsMatch != null && 2 in displayOptions -> {
                            val hits = hitsMatch.groupValues[1].toInt()
                            val tier = getTier(bossType)
                            val hitsColor = getHitsColor(hits, tier)
                            val hitsText = "$hitsColor$hits Hits"

                            when {
                                showName && useCompactDisplay -> displayLines.add("$nameWithTimer $hitsText")
                                showName -> {
                                    displayLines.add(nameWithTimer)
                                    displayLines.add(hitsText)
                                }
                                laserTimer.isNotEmpty() && useCompactDisplay -> displayLines.add("$laserTimer $hitsText")
                                laserTimer.isNotEmpty() -> {
                                    displayLines.add(laserTimer)
                                    displayLines.add(hitsText)
                                }
                                else -> displayLines.add(hitsText)
                            }
                        }
                    }

                    if (displayLines.isNotEmpty()) {
                        displayData[entityId] = displayLines.joinToString("\n")
                    }
                }
                false
            }
        }

        register<RenderEvent.LivingEntity.Post> { event ->
            if (!entities.contains(event.entity.entityId)) return@register

            displayData[event.entity.entityId]?.let { displayText ->
                Render3D.drawString(
                    displayText,
                    event.entity.positionVector.addVector(0.0, 0.25, 0.0),
                    Utils.partialTicks,
                    scaleMultiplier = 1.5f,
                    smallestDistanceView = 8.0,
                    yOff = 2.5f
                )
            }
        }

        configRegister<RenderEvent.World>("slayerdisplayshowkilltimer") {
            killTimers.values.forEach { timerData ->
                Render3D.drawString(
                    timerData.second,
                    timerData.third.addVector(0.0, 0.25, 0.0),
                    Utils.partialTicks,
                    scaleMultiplier = 1.5f,
                    yOff = 2.5f
                )
            }
        }
    }

    private fun formatTimerNametag(timerNametag: String): String {
        val match = timeRegex.find(timerNametag)
        return if (match != null) {
            val minutes = match.groupValues[1].toInt()
            val seconds = match.groupValues[2].toInt()
            val totalSeconds = minutes * 60 + seconds
            val color = getTimerColor(totalSeconds)
            val formattedMinutes = minutes.toString().padStart(2, '0')
            val formattedSeconds = seconds.toString().padStart(2, '0')
            "$color$formattedMinutes:$formattedSeconds"
        } else ""
    }

    private fun getTimerColor(totalSeconds: Int): String = when {
        totalSeconds > 180 -> "§a"
        totalSeconds > 120 -> "§e"
        totalSeconds > 60 -> "§6"
        else -> "§c"
    }

    private fun parseHp(hp: String): Long {
        return when {
            hp.endsWith("k", true) -> hp.dropLast(1).toDouble() * 1_000
            hp.endsWith("m", true) -> hp.dropLast(1).toDouble() * 1_000_000
            hp.endsWith("b", true) -> hp.dropLast(1).toDouble() * 1_000_000_000
            else -> hp.replace(",", "").toDouble()
        }.toLong()
    }

    private fun getHpColor(currentHp: Long, maxHp: Int): String = when (currentHp.toFloat() / maxHp.toFloat()) {
        in 0.75f..1f -> "§a"
        in 0.5f..0.75f -> "§e"
        in 0.25f..0.5f -> "§6"
        else -> "§c"
    }

    private fun getBossType(cleanName: String): BossTypes? {
        val slayerMatch = slayerMobRegex.find(cleanName.removeFormatting().replace(",", ""))
        if (slayerMatch != null) {
            val matchedName = slayerMatch.value
            val baseName = matchedName.substringBeforeLast(" ")
            val tierRoman = matchedName.substringAfterLast(" ")
            val tier = Utils.decodeRoman(tierRoman).toString()

            return BossTypes.entries.find { boss ->
                val bossClean = boss.fullName.removeFormatting()
                bossClean.contains(baseName, ignoreCase = true) && bossClean.contains(tier, ignoreCase = true)
            }
        }
        return null
    }

    private fun getTier(bossType: BossTypes): Int {
        return bossType.name.takeLast(1).toIntOrNull() ?: 1
    }

    private fun getHitsColor(currentHits: Int, tier: Int): String {
        val maxHits = when (tier) {
            4 -> 100
            3 -> 60
            2 -> 30
            1 -> 15
            else -> 100
        }
        return when (currentHits.toFloat() / maxHits) {
            in 0.75f..1f -> "§a"
            in 0.5f..0.75f -> "§e"
            in 0.25f..0.5f -> "§6"
            else -> "§c"
        }
    }

    private fun getSlayerPhaseText(bossType: BossTypes, health: Long, maxHealth: Int): String {
        if (3 !in displayOptions) return ""

        val tier = getTier(bossType)
        val bossName = bossType.name

        return when {
            bossName.startsWith("BLAZE") -> {
                val phases = if (tier <= 2) 2 else 3
                val step = maxHealth / phases
                val currentPhase = when {
                    health > step * (phases - 1) -> 1
                    health > step * (phases - 2) -> 2
                    else -> phases
                }
                val phaseColor = if (currentPhase == 1) "§c" else if (currentPhase == phases) "§a" else "§e"
                "$phaseColor$currentPhase/$phases "
            }
            bossName.startsWith("ENDERMAN") -> {
                val phases = if (tier == 4) 6 else 3
                val step = maxHealth / phases
                val currentPhase = when {
                    health > step * (phases - 1) -> 1
                    health > step * (phases - 2) -> 2
                    health > step * (phases - 3) && phases > 3 -> 3
                    health > step * (phases - 4) && phases > 4 -> 4
                    health > step * (phases - 5) && phases > 5 -> 5
                    else -> phases
                }
                val phaseColor = if (currentPhase == 1) "§c" else if (currentPhase == phases) "§a" else "§e"
                "$phaseColor$currentPhase/$phases "
            }
            bossName.startsWith("SPIDER") && tier == 5 -> {
                if (health > maxHealth / 2) "§e1/2 " else "§e2/2 "
            }
            else -> ""
        }
    }

    private fun getLaserTimer(entity: Entity): String {
        if (4 !in displayOptions) return ""

        val ridingEntity = entity.ridingEntity ?: return ""
        val totalTime = 8.2
        val time = maxOf(0.0, totalTime - (ridingEntity.ticksExisted / 20.0))
        val color = when {
            time > 6.0 -> "§a"
            time > 3.0 -> "§e"
            time > 1.0 -> "§6"
            else -> "§c"
        }
        return "$color${"%.1f".format(time)}s"
    }
}