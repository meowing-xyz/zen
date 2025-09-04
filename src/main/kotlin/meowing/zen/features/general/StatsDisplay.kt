package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.api.PlayerStats
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.elements.MCColorCode
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.GameEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.configRegister
import meowing.zen.features.Feature
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import meowing.zen.utils.Render2D.width
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.RenderGameOverlayEvent
import java.awt.Color

@Zen.Module
object StatsDisplay : Feature("statsdisplay") {
    private const val healthBarName = "Health Bar"
    private const val manaBarName = "Mana Bar"
    private const val overflowManaName = "Overflow Mana"
    private const val riftTimeBarName = "Rift Time Bar"
    private const val drillFuelBarName = "Drill Fuel Bar"

    private val hiddenstats by ConfigDelegate<Set<Int>>("hiddenstats")

    private val showHealthText by ConfigDelegate<Boolean>("showhealthtext")
    private val showMaxHealth by ConfigDelegate<Boolean>("showmaxhealth")
    private val healthTextColor by ConfigDelegate<MCColorCode>("healthtextcolor")
    private val maxHealthTextColor by ConfigDelegate<MCColorCode>("maxhealthtextcolor")

    private val showManaText by ConfigDelegate<Boolean>("showmanatext")
    private val showMaxMana by ConfigDelegate<Boolean>("showmaxmana")
    private val manaTextColor by ConfigDelegate<MCColorCode>("manatextcolor")
    private val maxManaTextColor by ConfigDelegate<MCColorCode>("maxmanatextcolor")

    private val showOverflowManaText by ConfigDelegate<Boolean>("showoverflowmanatext")
    private val overflowManaTextColor by ConfigDelegate<MCColorCode>("overflowmanatextcolor")

    private val showRiftTimeText by ConfigDelegate<Boolean>("showrifttimetext")
    private val riftTimeTextColor by ConfigDelegate<MCColorCode>("rifttimetextcolor")

    private val showDrillFuelText by ConfigDelegate<Boolean>("showdrillfueltext")
    private val showMaxDrillFuel by ConfigDelegate<Boolean>("showmaxdrillfuel")
    private val drillFuelTextColor by ConfigDelegate<MCColorCode>("drillfueltextcolor")
    private val maxDrillFuelTextColor by ConfigDelegate<MCColorCode>("maxdrillfueltextcolor")

    private enum class StatType(val displayName: String, val regex: Regex) {
        HEALTH("Health", """(§.)(?<currentHealth>[\d,]+)/(?<maxHealth>[\d,]+)❤""".toRegex()),
        MANA("Mana", """§b(?<currentMana>[\d,]+)/(?<maxMana>[\d,]+)✎( Mana)?""".toRegex()),
        OVERFLOW_MANA("Overflow Mana", """§3(?<overflowMana>[\d,]+)ʬ""".toRegex()),
        DEFENSE("Defense", """§a(?<defense>[\d,]+)§a❈ Defense""".toRegex()),
        RIFT_TIME("Rift Time", """(§7|§a)(?:(?<minutes>\d+)m\s*)?(?<seconds>\d+)sф Left""".toRegex()),
        DRILL_FUEL("Drill Fuel", """§2(?<currentFuel>[\d,]+)/(?<maxFuel>[\d,k]+) Drill Fuel""".toRegex()),
    }

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Stats Display", ConfigElement(
                "statsdisplay",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Stats Display", "Options", ConfigElement(
                "cleanactionbar",
                "Clean Action Bar",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Options", ConfigElement(
                "hidevanillahp",
                "Hide Vanilla HP and Saturation",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Options", ConfigElement(
                "hidevanillaarmor",
                "Hide Armor Icon",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Options", ConfigElement(
                "hideexpbar",
                "Hide Experience Bar",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Options", ConfigElement(
                "hiddenstats",
                "Hide Stats",
                ElementType.MultiCheckbox(
                    options = StatType.entries.map { it.displayName },
                    default = emptySet()
                )
            ))
            .addElement("General", "Stats Display", "Health Bar", ConfigElement(
                "showhealthtext",
                "Show Health Numbers",
                ElementType.Switch(true)
            ))
            .addElement("General", "Stats Display", "Health Bar", ConfigElement(
                "showmaxhealth",
                "Show Max Health",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Health Bar", ConfigElement(
                "healthtextcolor",
                "Health Text Color",
                ElementType.MCColorPicker(MCColorCode.WHITE)
            ))
            .addElement("General", "Stats Display", "Health Bar", ConfigElement(
                "maxhealthtextcolor",
                "Max Health Text Color",
                ElementType.MCColorPicker(MCColorCode.GRAY)
            ))
            .addElement("General", "Stats Display", "Mana Bar", ConfigElement(
                "showmanatext",
                "Show Mana Numbers",
                ElementType.Switch(true)
            ))
            .addElement("General", "Stats Display", "Mana Bar", ConfigElement(
                "showmaxmana",
                "Show Max Mana",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Mana Bar", ConfigElement(
                "manatextcolor",
                "Mana Text Color",
                ElementType.MCColorPicker(MCColorCode.BLUE)
            ))
            .addElement("General", "Stats Display", "Mana Bar", ConfigElement(
                "maxmanatextcolor",
                "Max Mana Text Color",
                ElementType.MCColorPicker(MCColorCode.DARK_BLUE)
            ))
            .addElement("General", "Stats Display", "Overflow Mana", ConfigElement(
                "showoverflowmanatext",
                "Show Overflow Mana",
                ElementType.Switch(true)
            ))
            .addElement("General", "Stats Display", "Overflow Mana", ConfigElement(
                "overflowmanatextcolor",
                "Overflow Mana Text Color",
                ElementType.MCColorPicker(MCColorCode.DARK_AQUA)
            ))
            .addElement("General", "Stats Display", "Rift Time Bar", ConfigElement(
                "showrifttimetext",
                "Show Rift Time Text",
                ElementType.Switch(true)
            ))
            .addElement("General", "Stats Display", "Rift Time Bar", ConfigElement(
                "rifttimetextcolor",
                "Rift Time Text Color",
                ElementType.MCColorPicker(MCColorCode.GREEN)
            ))
            .addElement("General", "Stats Display", "Drill Fuel Bar", ConfigElement(
                "showdrillfueltext",
                "Show Drill Fuel Numbers",
                ElementType.Switch(true)
            ))
            .addElement("General", "Stats Display", "Drill Fuel Bar", ConfigElement(
                "showmaxdrillfuel",
                "Show Max Drill Fuel",
                ElementType.Switch(false)
            ))
            .addElement("General", "Stats Display", "Drill Fuel Bar", ConfigElement(
                "drillfueltextcolor",
                "Drill Fuel Text Color",
                ElementType.MCColorPicker(MCColorCode.DARK_GREEN)
            ))
            .addElement("General", "Stats Display", "Drill Fuel Bar", ConfigElement(
                "maxdrillfueltextcolor",
                "Max Drill Fuel Text Color",
                ElementType.MCColorPicker(MCColorCode.GREEN)
            ))
    }

    override fun initialize() {
        HUDManager.register(overflowManaName, "§3642ʬ")
        HUDManager.registerCustom(healthBarName, 80, 10, this::healthBarEditorRender)
        HUDManager.registerCustom(manaBarName, 80, 10, this::manaBarEditorRender)
        HUDManager.registerCustom(riftTimeBarName, 80, 10, this::riftTimeBarEditorRender)
        HUDManager.registerCustom(drillFuelBarName, 80, 10, this::drillFuelBarEditorRender)

        configRegister<GameEvent.ActionBar>(listOf("statsdisplay", "cleanactionbar"), priority = 1000) { event ->
            val actionBar = hiddenstats.fold(event.event.message.formattedText) { text, index ->
                StatType.entries.getOrNull(index)?.regex?.replace(text, "") ?: text
            }
            event.event.message = ChatComponentText(actionBar.trim().replace("§r  ", " "))
        }

        configRegister<RenderEvent.HUD>(listOf("statsdisplay", "hidevanillahp"), priority = 1000) { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.HEALTH || event.elementType == RenderGameOverlayEvent.ElementType.FOOD) event.cancel()
        }

        configRegister<RenderEvent.HUD>(listOf("statsdisplay", "hidevanillaarmor"), priority = 1000) { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.ARMOR) event.cancel()
        }

        configRegister<RenderEvent.HUD>(listOf("statsdisplay", "hideexpbar"), priority = 1000) { event ->
            if (event.elementType == RenderGameOverlayEvent.ElementType.EXPERIENCE) event.cancel()
        }

        register<RenderEvent.Text> {
            renderHealthBar()
            renderManaBar()
            renderOverflowMana()
            renderRiftTimeBar()
            renderDrillFuelBar()
        }
    }

    private fun renderBar(x: Float, y: Float, width: Int, height: Int, scale: Float, primaryFill: Double, primaryColor: Color, secondaryFill: Double = 0.0, secondaryColor: Color? = null) {
        val scaledWidth = width * scale
        val scaledHeight = height * scale
        val borderWidth = 1f * scale
        val fillHeight = 8f * scale
        val radius = 2f * scale

        Render2D.drawRoundedRect(Color.BLACK, x, y, scaledWidth, scaledHeight, radius)
        Render2D.drawRoundedRect(Color.DARK_GRAY, x + borderWidth, y + borderWidth, scaledWidth - 2 * borderWidth, fillHeight, radius * 0.75f)

        val availableWidth = scaledWidth - 2 * borderWidth
        val primaryWidth = (availableWidth * primaryFill).toFloat()
        val secondaryWidth = (availableWidth * secondaryFill).toFloat()

        if (primaryWidth > 0) {
            if (secondaryFill > 0 && secondaryColor != null) {
                Render2D.drawRoundedRectLeft(primaryColor, x + borderWidth, y + borderWidth, primaryWidth, fillHeight, radius * 0.75f)
            } else {
                Render2D.drawRoundedRect(primaryColor, x + borderWidth, y + borderWidth, primaryWidth, fillHeight, radius * 0.75f)
            }
        }

        if (secondaryFill > 0 && secondaryColor != null && secondaryWidth > 0) {
            Render2D.drawRoundedRectRight(secondaryColor, x + borderWidth + primaryWidth, y + borderWidth, secondaryWidth, fillHeight, radius * 0.75f)
        }
    }

    private fun renderText(text: String, x: Float, y: Float, width: Int, scale: Float) {
        val textWidth = text.width() * scale
        val scaledWidth = width * scale
        val centerX = x + scaledWidth / 2f
        val textX = centerX - textWidth / 2f
        val textY = y - 8f * scale
        Render2D.renderStringWithShadow(text, textX, textY, scale)
    }

    private fun renderHealthBar() {
        if (!HUDManager.isEnabled(healthBarName) || PlayerStats.maxHealth == 0) return
        val max = PlayerStats.maxHealth
        val absorption = PlayerStats.absorption
        val health = PlayerStats.displayedHealth
        val total = max + absorption
        val healthFillPerc = health.toDouble() / total
        val absorbFillPerc = absorption.toDouble() / total
        val x = HUDManager.getX(healthBarName)
        val y = HUDManager.getY(healthBarName)
        val scale = HUDManager.getScale(healthBarName)

        healthBarEditorRender(x, y, 80, 10, scale, 0f, false, healthFillPerc, absorbFillPerc)
    }

    private fun renderManaBar() {
        if (!HUDManager.isEnabled(manaBarName) || PlayerStats.maxMana == 0) return
        val max = PlayerStats.maxMana
        val current = PlayerStats.displayedMana
        val fillPerc = current.toDouble() / max
        val x = HUDManager.getX(manaBarName)
        val y = HUDManager.getY(manaBarName)
        val scale = HUDManager.getScale(manaBarName)

        manaBarEditorRender(x, y, 80, 10, scale, 0f, false, fillPerc)
    }

    private fun renderOverflowMana() {
        if (!HUDManager.isEnabled(overflowManaName)) return
        val x = HUDManager.getX(overflowManaName)
        val y = HUDManager.getY(overflowManaName)
        val scale = HUDManager.getScale(overflowManaName)

        if (showOverflowManaText) {
            val overflowMana = PlayerStats.overflowMana

            if (overflowMana > 0) {
                val overflowText = "${overflowManaTextColor.code}${overflowMana}ʬ"
                renderText(overflowText, x, y, overflowText.width(), scale)
            }
        }
    }

    private fun renderRiftTimeBar() {
        if (!HUDManager.isEnabled(riftTimeBarName) || PlayerStats.maxRiftTime == 0) return
        val current = PlayerStats.riftTimeSeconds
        val max = PlayerStats.maxRiftTime
        val fillPerc = current.toDouble() / max
        val x = HUDManager.getX(riftTimeBarName)
        val y = HUDManager.getY(riftTimeBarName)
        val scale = HUDManager.getScale(riftTimeBarName)

        riftTimeBarEditorRender(x, y, 80, 10, scale, 0f, false, fillPerc)
    }

    private fun renderDrillFuelBar() {
        if (!HUDManager.isEnabled(drillFuelBarName) || PlayerStats.maxDrillFuel == 0) return
        val max = PlayerStats.maxDrillFuel
        val current = PlayerStats.drillFuel
        val fillPerc = current.toDouble() / max
        val x = HUDManager.getX(drillFuelBarName)
        val y = HUDManager.getY(drillFuelBarName)
        val scale = HUDManager.getScale(drillFuelBarName)

        drillFuelBarEditorRender(x, y, 80, 10, scale, 0f, false, fillPerc)
    }

    fun healthBarEditorRender(x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean, healthPerc: Double = 0.75, absorbPerc: Double = 0.25) {
        renderBar(x, y, width, height, scale, healthPerc, Color.RED, absorbPerc, Color.YELLOW)

        if (showHealthText || previewMode) {
            val currentHealth = if (previewMode) {
                (1000 * (healthPerc + absorbPerc)).toInt()
            } else {
                PlayerStats.health + PlayerStats.absorption
            }

            val healthText = if (showMaxHealth) {
                val maxValue = if (previewMode) 1000 else PlayerStats.maxHealth
                "${healthTextColor.code}$currentHealth§8/${maxHealthTextColor.code}$maxValue"
            } else {
                "${healthTextColor.code}$currentHealth"
            }

            renderText(healthText, x, y, width, scale)
        }
    }

    fun manaBarEditorRender(x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean, manaPerc: Double = 0.6) {
        renderBar(x, y, width, height, scale, manaPerc, Color.BLUE)

        if (showManaText || previewMode) {
            val currentMana = if (previewMode) {
                (1000 * manaPerc).toInt()
            } else {
                PlayerStats.mana
            }

            val manaText = if (showMaxMana) {
                val maxValue = if (previewMode) 1000 else PlayerStats.maxMana
                "${manaTextColor.code}$currentMana§8/${maxManaTextColor.code}$maxValue"
            } else {
                "${manaTextColor.code}$currentMana"
            }

            renderText(manaText, x, y, width, scale)
        }
    }

    fun riftTimeBarEditorRender(x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean, timePerc: Double = 0.8) {
        renderBar(x, y, width, height, scale, timePerc, Color.GREEN)

        if (showRiftTimeText || previewMode) {
            val timeValue = if (previewMode) {
                "48m 32s"
            } else {
                val minutes = PlayerStats.riftTimeSeconds / 60
                val seconds = PlayerStats.riftTimeSeconds % 60
                "${minutes}m ${seconds}s"
            }

            val timeText = "${riftTimeTextColor.code}$timeValue"
            renderText(timeText, x, y, width, scale)
        }
    }

    fun drillFuelBarEditorRender(x: Float, y: Float, width: Int, height: Int, scale: Float, partialTicks: Float, previewMode: Boolean, fuelPerc: Double = 0.7) {
        renderBar(x, y, width, height, scale, fuelPerc, Color(0, 128, 0))

        if (showDrillFuelText || previewMode) {
            val currentFuel = if (previewMode) {
                (1000 * fuelPerc).toInt()
            } else {
                PlayerStats.drillFuel
            }

            val fuelText = if (showMaxDrillFuel) {
                val maxValue = if (previewMode) 1000 else PlayerStats.maxDrillFuel
                "${drillFuelTextColor.code}$currentFuel§8/${maxDrillFuelTextColor.code}$maxValue"
            } else "${drillFuelTextColor.code}$currentFuel"

            renderText(fuelText, x, y, width, scale)
        }
    }
}