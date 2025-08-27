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
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3

// TODO: fix for blaze (doesn't track past phase 1)
@Zen.Module
object SlayerDisplay : Feature("slayerdisplay") {
    private var slayerEntities = mutableMapOf<Int, SlayerData>()
    private var nametagData = mutableMapOf<Int, String>()
    private var killTimers = mutableMapOf<Int, Triple<Long, String, Vec3>>()
    private var hiddenArmorStands = mutableSetOf<Int>()

    private val slayerMobRegex = "(?<=☠\\s)[A-Za-z]+\\s[A-Za-z]+(?:\\s[IVX]+)?".toRegex()
    private val hpRegex = "(\\d+(?:,\\d{3})*(?:\\.\\d+)?[kmb]?)❤".toRegex(RegexOption.IGNORE_CASE)
    private val hitsRegex = "(\\d+)\\s+Hits".toRegex()
    private val timeRegex = "(\\d+):(\\d+)".toRegex()

    private val useFullName by ConfigDelegate<Boolean>("slayerdisplayusefullname")
    private val displayOptions by ConfigDelegate<Set<Int>>("slayerdisplayoptions")
    private val shownBossesOption by ConfigDelegate<Int>("slayerdisplaybossshown")
    private val showKillTimer by ConfigDelegate<Boolean>("slayerdisplayshowkilltimer")
    private val hideOriginalNametags by ConfigDelegate<Boolean>("slayerdisplayhideoriginalnametags")

    private data class SlayerData(
        val spawnTime: Long,
        var displayText: String = "",
        var bossType: BossTypes? = null
    )

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
                "slayerdisplayhideoriginalnametags",
                "Hide Original Nametags",
                ElementType.Switch(false)
            ))
            .addElement("Slayers", "Slayer Display", "Options", ConfigElement(
                "slayerdisplayoptions",
                "Display Options",
                ElementType.MultiCheckbox(
                    listOf(
                        "Show Mob Name",
                        "Show Health",
                        "Show Hits",
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
                ElementType.Dropdown(
                    listOf(
                        "Show for all bosses",
                        "Only for carries",
                        "Only for mine",
                        "Mine and carries"
                    ),
                    0
                )
            ))
    }

    override fun initialize() {
        register<EntityEvent.Metadata> { event ->
            val cleanName = event.name.removeFormatting().replace(",", "")
            val entityId = event.entity.entityId

            nametagData[entityId] = cleanName

            if (slayerMobRegex.find(cleanName) != null) {
                val slayerEntityId = event.packet.entityId - 1
                if (!slayerEntities.containsKey(slayerEntityId)) {
                    slayerEntities[slayerEntityId] = SlayerData(System.currentTimeMillis())
                }

                if (hideOriginalNametags && shouldShowSlayer(slayerEntityId)) {
                    hiddenArmorStands.add(entityId)
                    hiddenArmorStands.add(entityId + 1)
                    hiddenArmorStands.add(entityId + 2)
                }
            }

            if (hideOriginalNametags && isSlayerRelatedNametag(entityId)) {
                val possibleSlayerEntityId = entityId - 1
                if (slayerEntities.containsKey(possibleSlayerEntityId) && shouldShowSlayer(possibleSlayerEntityId)) {
                    hiddenArmorStands.add(entityId)
                }
            }

            updateSlayerDisplay(entityId, cleanName)
        }

        configRegister<RenderEvent.LivingEntity.Pre>("slayerdisplayhideoriginalnametags") { event ->
            if (event.entity is EntityArmorStand && hiddenArmorStands.contains(event.entity.entityId)) {
                event.cancel()
            }
        }

        configRegister<TickEvent.Client>("slayerdisplayoptions", 3) {
            slayerEntities.forEach { (slayerEntityId, _) ->
                val nametagEntityId = slayerEntityId + 1
                val cleanName = nametagData[nametagEntityId] ?: return@forEach
                updateSlayerDisplay(nametagEntityId, cleanName)
            }
        }

        register<EntityEvent.Leave> { event ->
            val entityId = event.entity.entityId
            slayerEntities[entityId]?.let { slayerData ->
                if (showKillTimer) {
                    val killTime = System.currentTimeMillis() - slayerData.spawnTime
                    val seconds = killTime / 1000.0
                    val killTimeText = "§a${String.format("%.1f", seconds)}s"
                    val position = event.entity.positionVector
                    killTimers[entityId] = Triple(System.currentTimeMillis(), killTimeText, position)
                }
            }
            slayerEntities.remove(entityId)
            nametagData.remove(entityId)
            hiddenArmorStands.remove(entityId)
            hiddenArmorStands.remove(entityId + 1)
            hiddenArmorStands.remove(entityId + 2)
            hiddenArmorStands.remove(entityId + 3)
        }

        register<RenderEvent.LivingEntity.Post> { event ->
            slayerEntities[event.entity.entityId]?.displayText?.takeIf { it.isNotEmpty() }?.let { displayText ->
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
            killTimers.entries.removeAll { (_, timerData) ->
                val expired = System.currentTimeMillis() - timerData.first > 3000
                if (!expired) {
                    Render3D.drawString(
                        timerData.second,
                        timerData.third.addVector(0.0, 0.25, 0.0),
                        Utils.partialTicks,
                        scaleMultiplier = 1.5f,
                        yOff = 2.5f
                    )
                }
                expired
            }
        }
    }

    private fun isSlayerRelatedNametag(entityId: Int): Boolean {
        val name = nametagData[entityId] ?: return false
        return name.contains("Hits") || name.contains(":") || name.contains("Spawned by")
    }

    private fun updateSlayerDisplay(nametagEntityId: Int, cleanName: String) {
        val slayerEntityId = nametagEntityId - 1
        val slayerData = slayerEntities[slayerEntityId] ?: return

        if (!shouldShowSlayer(slayerEntityId)) {
            slayerData.displayText = ""
            return
        }

        val bossType = getBossType(cleanName)
        if (bossType != null) {
            slayerData.bossType = bossType
        }

        val actualBossType = slayerData.bossType ?: return
        val entity = world?.getEntityByID(slayerEntityId) ?: return

        val hpMatch = hpRegex.find(cleanName)
        val hitsMatch = hitsRegex.find(cleanName)
        val timerNametag = nametagData[nametagEntityId + 1] ?: ""

        if ((hpMatch != null && 1 in displayOptions) || (hitsMatch != null && 2 in displayOptions)) {
            val prefix = listOfNotNull(
                cleanName.takeIf { it.contains("✯") }?.let { "§b✯§r" },
                cleanName.takeIf { it.contains("✩") }?.let { "§5✩§r" }
            ).joinToString(" ")
            slayerData.displayText = buildDisplayText(entity, actualBossType, hpMatch, hitsMatch, timerNametag, prefix)
        }
    }

    private fun shouldShowSlayer(slayerEntityId: Int): Boolean {
        val spawnerNametag = nametagData[slayerEntityId + 3] ?: ""

        return when (shownBossesOption) {
            0 -> true
            1 -> spawnerNametag.contains("Spawned by") && CarryCounter.carryees.any { spawnerNametag.endsWith("by: ${it.name}") }
            2 -> spawnerNametag.contains("Spawned by") && spawnerNametag.endsWith("by: ${player?.name}")
            3 -> spawnerNametag.contains("Spawned by") && (spawnerNametag.endsWith("by: ${player?.name}") || CarryCounter.carryees.any { spawnerNametag.endsWith("by: ${it.name}") })
            else -> false
        }
    }

    private fun buildDisplayText(
        entity: Entity,
        bossType: BossTypes,
        hpMatch: MatchResult?,
        hitsMatch: MatchResult?,
        timerNametag: String,
        prefix: String = ""
    ): String {
        val baseName = if (useFullName) bossType.fullName else bossType.shortName
        val mobName = if (prefix.isNotEmpty()) "$prefix$baseName" else baseName
        val laserTimer = getLaserTimer(entity)
        val useCompactDisplay = 5 in displayOptions
        val showName = 0 in displayOptions
        val displayLines = mutableListOf<String>()

        val nameWithTimer = if (laserTimer.isNotEmpty()) "$mobName $laserTimer" else mobName

        if (4 in displayOptions && timerNametag.isNotEmpty()) {
            formatTimerNametag(timerNametag).takeIf { it.isNotEmpty() }?.let {
                displayLines.add(it)
            }
        }

        when {
            hpMatch != null && 1 in displayOptions -> {
                val currentHp = parseHp(hpMatch.groupValues[1])
                val maxHp = (entity as EntityLivingBase).baseMaxHealth
                val hpColor = getHpColor(currentHp, maxHp)
                val healthText = "$hpColor${hpMatch.groupValues[1]}"

                when {
                    showName && useCompactDisplay -> displayLines.add("$nameWithTimer $healthText")
                    showName -> {
                        displayLines.add(nameWithTimer)
                        displayLines.add(healthText)
                    }
                    laserTimer.isNotEmpty() && useCompactDisplay -> displayLines.add("$laserTimer $healthText")
                    laserTimer.isNotEmpty() -> {
                        displayLines.add(laserTimer)
                        displayLines.add(healthText)
                    }
                    else -> displayLines.add(healthText)
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

        return displayLines.joinToString("\n")
    }

    private fun formatTimerNametag(timerNametag: String): String {
        val match = timeRegex.find(timerNametag) ?: return ""
        val minutes = match.groupValues[1].toInt()
        val seconds = match.groupValues[2].toInt()
        val totalSeconds = minutes * 60 + seconds
        val color = getTimerColor(totalSeconds)
        val formattedMinutes = minutes.toString().padStart(2, '0')
        val formattedSeconds = seconds.toString().padStart(2, '0')
        return "$color$formattedMinutes:$formattedSeconds"
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
        val slayerMatch = slayerMobRegex.find(cleanName.removeFormatting().replace(",", "")) ?: return null
        val matchedName = slayerMatch.value
        val baseName = matchedName.substringBeforeLast(" ")
        val tierRoman = matchedName.substringAfterLast(" ")
        val tier = Utils.decodeRoman(tierRoman).toString()

        return BossTypes.entries.find { boss ->
            val bossClean = boss.fullName.removeFormatting()
            bossClean.contains(baseName, ignoreCase = true) && bossClean.contains(tier, ignoreCase = true)
        }
    }

    private fun getTier(bossType: BossTypes): Int = bossType.name.takeLast(1).toIntOrNull() ?: 1

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

    private fun getLaserTimer(entity: Entity): String {
        if (3 !in displayOptions) return ""

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