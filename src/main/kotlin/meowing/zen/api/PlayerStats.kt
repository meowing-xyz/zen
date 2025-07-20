package meowing.zen.api

import meowing.zen.Zen
import meowing.zen.events.ChatEvent
import meowing.zen.events.EventBus
import meowing.zen.events.TickEvent
import meowing.zen.events.WorldEvent
import kotlin.math.max

// TODO: Add interpolation and action bar cleaning in the future
@Zen.Module
object PlayerStats {
    private var HEALTH_REGEX = """(§.)(?<currentHealth>[\d,]+)/(?<maxHealth>[\d,]+)❤""".toRegex()
    var health = 0
    var maxHealth = 0
    var absorption = 0

    private var MANA_REGEX = """§b(?<currentMana>[\d,]+)/(?<maxMana>[\d,]+)✎( Mana)?""".toRegex()
    private var OVERFLOW_REGEX = """§3(?<overflowMana>[\d,]+)ʬ""".toRegex()
    var mana = 0
    var maxMana = 0
    var overflowMana = 0

    private var DEFENSE_REGEX = """§a(?<defense>[\d,]+)§a❈ Defense""".toRegex()
    var defense = 0
    var effectiveHealth = 0
    var maxEffectiveHealth = 0

    private var RIFT_REGEX = """(§7|§a)(?:(?<minutes>\d+)m\s*)?(?<seconds>\d+)sф Left""".toRegex()
    var maxRiftTime = 0
    var riftTimeSeconds = 0

    private var DRILL_FUEL_REGEX = """§2(?<currentFuel>[\d,]+)/(?<maxFuel>[\d,k]+) Drill Fuel""".toRegex()
    var drillFuel = 0
    var maxDrillFuel = 0

    private var DUNGEON_SECRETS_REGEX = """§7(?<secrets>[\d,]+)/(?<maxSecrets>[\d,]+) Secrets§r""".toRegex()
    var currentRoomSecrets = 0
    var currentRoomMaxSecrets = 0

    var displayedHealth = health.toFloat()
    var displayedMana = mana.toFloat()

    // For health and mana interpolation, refer to https://github.com/MrFast-js/Skyblock-Tweaks/blob/main/src/main/java/mrfast/sbt/apis/PlayerStats.kt#L75
    private var lastHealth = health.toFloat()
    private var lastMana = mana.toFloat()

    private var healthRegenPerInterval = 0f
    private var manaRegenPerInterval = 0f

    init {
        EventBus.register<WorldEvent.Load>({ event ->
            maxRiftTime = 0
            currentRoomSecrets = -1
            currentRoomMaxSecrets = 0
        })

        EventBus.register<TickEvent.Client>({ event ->
            displayedMana = mana.toFloat()
            displayedHealth = health.toFloat()
        })

        EventBus.register<ChatEvent.Receive>({ event ->
            if (event.event.type.toInt() == 2) {
                val actionBar: String = event.event.message.formattedText

                extractPlayerStats(actionBar)
            }
        })
    }

    private fun extractPlayerStats(filledActionBar: String) {
        val actionBar = filledActionBar.replace(",", "").replace("k", "000")

        if (HEALTH_REGEX.containsMatchIn(actionBar)) {
            val groups = HEALTH_REGEX.find(actionBar)?.groups ?: return
            health = groups["currentHealth"]!!.value.toInt()
            maxHealth = groups["maxHealth"]!!.value.toInt()
            effectiveHealth = (health * (1 + defense / 100))
            maxEffectiveHealth = (maxHealth * (1 + defense / 100))
            absorption = max(health - maxHealth, 0)
        }

        if (DRILL_FUEL_REGEX.containsMatchIn(actionBar)) {
            val groups = DRILL_FUEL_REGEX.find(actionBar)?.groups ?: return
            drillFuel = groups["currentFuel"]!!.value.toInt()
            maxDrillFuel = groups["maxFuel"]!!.value.toInt()
        }

        if (DUNGEON_SECRETS_REGEX.containsMatchIn(actionBar)) {
            val groups = DUNGEON_SECRETS_REGEX.find(actionBar)?.groups ?: return
            currentRoomSecrets = groups["secrets"]!!.value.toInt()
            currentRoomMaxSecrets = groups["maxSecrets"]!!.value.toInt()
        }

        if (MANA_REGEX.containsMatchIn(actionBar)) {
            val groups = MANA_REGEX.find(actionBar)?.groups ?: return
            mana = groups["currentMana"]!!.value.toInt()
            maxMana = groups["maxMana"]!!.value.toInt()
        }

        if (OVERFLOW_REGEX.containsMatchIn(actionBar)) {
            val groups = OVERFLOW_REGEX.find(actionBar)?.groups ?: return
            overflowMana = groups["overflowMana"]!!.value.toInt()
        }

        if (DEFENSE_REGEX.containsMatchIn(actionBar)) {
            val groups = DEFENSE_REGEX.find(actionBar)?.groups ?: return
            defense = groups["defense"]!!.value.toInt()
        }

        if (RIFT_REGEX.containsMatchIn(actionBar)) {
            val groups = RIFT_REGEX.find(actionBar)?.groups ?: return
            val minutes = groups["minutes"]?.value?.toInt() ?: 0
            val seconds = groups["seconds"]!!.value.toInt()

            riftTimeSeconds = minutes * 60 + seconds
            if (riftTimeSeconds > maxRiftTime) maxRiftTime = riftTimeSeconds
        }
    }
}