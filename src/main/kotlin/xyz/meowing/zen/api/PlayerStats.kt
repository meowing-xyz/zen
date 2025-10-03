package xyz.meowing.zen.api

import xyz.meowing.zen.Zen
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.GameEvent
import xyz.meowing.zen.events.TickEvent
import xyz.meowing.zen.events.WorldEvent
import kotlin.math.abs
import kotlin.math.max

@Zen.Module
object PlayerStats {
    private val HEALTH_REGEX = """(§.)(?<currentHealth>[\d,]+)/(?<maxHealth>[\d,]+)❤""".toRegex()
    private val MANA_REGEX = """§b(?<currentMana>[\d,]+)/(?<maxMana>[\d,]+)✎( Mana)?""".toRegex()
    private val OVERFLOW_REGEX = """§3(?<overflowMana>[\d,]+)ʬ""".toRegex()
    private val DEFENSE_REGEX = """§a(?<defense>[\d,]+)§a❈ Defense""".toRegex()
    private val RIFT_REGEX = """(§7|§a)(?:(?<minutes>\d+)m\s*)?(?<seconds>\d+)sф Left""".toRegex()
    private val DRILL_FUEL_REGEX = """§2(?<currentFuel>[\d,]+)/(?<maxFuel>[\d,k]+) Drill Fuel""".toRegex()
    private val DUNGEON_SECRETS_REGEX = """§7(?<secrets>[\d,]+)/(?<maxSecrets>[\d,]+) Secrets""".toRegex()

    var health = 0
    var maxHealth = 0
    var absorption = 0
    var mana = 0
    var maxMana = 0
    var overflowMana = 0
    var defense = 0
    var effectiveHealth = 0
    var maxEffectiveHealth = 0
    var maxRiftTime = 0
    var riftTimeSeconds = 0
    var drillFuel = 0
    var maxDrillFuel = 0
    var currentRoomSecrets = 0
    var currentRoomMaxSecrets = 0

    var displayedHealth = 0f
    var displayedMana = 0f

    private var lastHealth = 0f
    private var lastMana = 0f
    private var healthRegenRate = 0f
    private var manaRegenRate = 0f

    init {
        EventBus.register<WorldEvent.Change> {
            maxRiftTime = 0
            currentRoomSecrets = -1
            currentRoomMaxSecrets = 0
            health = 0
            maxHealth = 0
            absorption = 0
            mana = 0
            maxMana = 0
            overflowMana = 0
            defense = 0
            effectiveHealth = 0
            maxEffectiveHealth = 0
            maxRiftTime = 0
            riftTimeSeconds = 0
            drillFuel = 0
            maxDrillFuel = 0
            currentRoomSecrets = 0
            currentRoomMaxSecrets = 0
            displayedHealth = 0f
            displayedMana = 0f
        }

        EventBus.register<TickEvent.Client> {
            if (health >= maxHealth || abs(displayedHealth - health) > 50) {
                resetInterpolation(health.toFloat(), true)
            }

            if (mana >= maxMana || abs(displayedMana - mana) > 50) {
                resetInterpolation(mana.toFloat(), false)
            }

            updateRegenRates()
            applyInterpolation()
        }

        EventBus.register<GameEvent.ActionBar> { event ->
            extractPlayerStats(event.event.message.formattedText)
        }
    }

    private fun resetInterpolation(value: Float, isHealth: Boolean) {
        if (isHealth) {
            displayedHealth = value
            lastHealth = value
        } else {
            displayedMana = value
            lastMana = value
        }
    }

    private fun updateRegenRates() {
        val currentHealth = health.toFloat()
        val currentMana = mana.toFloat()

        healthRegenRate = when {
            currentHealth < lastHealth -> { displayedHealth = currentHealth; 0f }
            currentHealth > lastHealth -> currentHealth - lastHealth
            else -> healthRegenRate
        }

        manaRegenRate = when {
            currentMana < lastMana -> { displayedMana = currentMana; 0f }
            currentMana > lastMana -> currentMana - lastMana
            else -> manaRegenRate
        }

        lastHealth = currentHealth
        lastMana = currentMana
    }

    private fun applyInterpolation() {
        displayedHealth = (displayedHealth + healthRegenRate / 20f).coerceAtMost(maxHealth.toFloat())
        displayedMana = (displayedMana + manaRegenRate / 20f).coerceAtMost(maxMana.toFloat())
    }

    private fun extractPlayerStats(actionBar: String) {
        val cleanBar = actionBar.replace(",", "").replace("k", "000")

        HEALTH_REGEX.find(cleanBar)?.let { match ->
            health = match.groups["currentHealth"]!!.value.toInt()
            maxHealth = match.groups["maxHealth"]!!.value.toInt()
            effectiveHealth = health * (1 + defense / 100)
            maxEffectiveHealth = maxHealth * (1 + defense / 100)
            absorption = max(health - maxHealth, 0)
        }

        MANA_REGEX.find(cleanBar)?.let { match ->
            mana = match.groups["currentMana"]!!.value.toInt()
            maxMana = match.groups["maxMana"]!!.value.toInt()
        }

        OVERFLOW_REGEX.find(cleanBar)?.let { match ->
            overflowMana = match.groups["overflowMana"]!!.value.toInt()
        }

        DEFENSE_REGEX.find(cleanBar)?.let { match ->
            defense = match.groups["defense"]!!.value.toInt()
        }

        DRILL_FUEL_REGEX.find(cleanBar)?.let { match ->
            drillFuel = match.groups["currentFuel"]!!.value.toInt()
            maxDrillFuel = match.groups["maxFuel"]!!.value.toInt()
        }

        DUNGEON_SECRETS_REGEX.find(cleanBar)?.let { match ->
            currentRoomSecrets = match.groups["secrets"]!!.value.toInt()
            currentRoomMaxSecrets = match.groups["maxSecrets"]!!.value.toInt()
        }

        RIFT_REGEX.find(cleanBar)?.let { match ->
            val minutes = match.groups["minutes"]?.value?.toInt() ?: 0
            val seconds = match.groups["seconds"]!!.value.toInt()
            riftTimeSeconds = minutes * 60 + seconds
            if (riftTimeSeconds > maxRiftTime) maxRiftTime = riftTimeSeconds
        }
    }
}