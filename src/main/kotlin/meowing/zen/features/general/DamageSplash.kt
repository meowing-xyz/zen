package meowing.zen.features.general

import gg.essential.universal.UChat.addColor
import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.elements.MCColorCode
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.SkyblockEvent
import meowing.zen.features.Feature
import meowing.zen.utils.Utils.format
import meowing.zen.utils.Utils.removeFormatting
import java.text.DecimalFormat

@Zen.Module
object DamageSplash : Feature("damagesplash") {
    enum class DamageType(val displayName: String) {
        CRIT("Crit Hits"),
        OVERLOAD("Overload Hits"),
        FIRE("Fire Hits"),
        NORMAL("Non-Crit Hits")
    }

    private val allSymbols = setOf('✧', '✯', '⚔', '+', '❤', '♞', '☄', '✷', 'ﬗ')
    private val commaFormatter = DecimalFormat("#,###")

    private val enableRainbow by ConfigDelegate<Boolean>("damagesplashrainbow")
    private val normalDamageColor by ConfigDelegate<MCColorCode>("damagesplashnormalcolor")
    private val criticalDamageColor by ConfigDelegate<MCColorCode>("damagesplashcriticalcolor")
    private val enabledColors by ConfigDelegate<Set<Int>>("damagesplashcolors")
    private val showFormatted by ConfigDelegate<Boolean>("damagesplashformatted")
    private val useCommas by ConfigDelegate<Boolean>("damagesplashcommas")
    private val cancelTypes by ConfigDelegate<Set<Int>>("damagesplashcancel")
    private val cancelAll by ConfigDelegate<Boolean>("damagesplashcancelall")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Damage Splash", ConfigElement(
                "damagesplash",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Damage Splash", "Display", ConfigElement(
                "damagesplashformatted",
                "Show formatted numbers",
                ElementType.Switch(true)
            ))
            .addElement("General", "Damage Splash", "Display", ConfigElement(
                "damagesplashcommas",
                "Use comma separators",
                ElementType.Switch(false)
            ))
            .addElement("General", "Damage Splash", "Cancellation", ConfigElement(
                "damagesplashcancelall",
                "Cancel all damage splash",
                ElementType.Switch(false)
            ))
            .addElement("General", "Damage Splash", "Cancellation", ConfigElement(
                "damagesplashcancel",
                "Cancel specific damage types",
                ElementType.MultiCheckbox(
                    options = DamageType.entries.map { it.displayName },
                    default = setOf()
                )
            ))
            .addElement("General", "Damage Splash", "Colors", ConfigElement(
                "damagesplashrainbow",
                "Rainbow damage with symbols",
                ElementType.Switch(true)
            ))
            .addElement("General", "Damage Splash", "Colors", ConfigElement(
                "damagesplashnormalcolor",
                "Normal damage color",
                ElementType.MCColorPicker(MCColorCode.AQUA)
            ))
            .addElement("General", "Damage Splash", "Colors", ConfigElement(
                "damagesplashcriticalcolor",
                "Symbol damage color",
                ElementType.MCColorPicker(MCColorCode.WHITE)
            ))
            .addElement("General", "Damage Splash", "Colors", ConfigElement(
                "damagesplashcolors",
                "Rainbow colors to use",
                ElementType.MultiCheckbox(
                    options = listOf("Gold", "Red", "Yellow", "White", "Green", "Aqua", "Light Purple", "Blue"),
                    default = setOf(0, 1, 2, 3)
                )
            ))
    }

    override fun initialize() {
        register<SkyblockEvent.DamageSplash>(priority = 1000) { event ->
            if (cancelAll) return@register event.cancel()

            val nameObj = event.packet.func_149027_c()?.find { it.objectType == 4 } ?: return@register
            val name = event.originalName.removeFormatting()

            if (cancelTypes.contains(detectDamageType(event.originalName, name).ordinal)) return@register event.cancel()

            val detectedSymbols = name.filter { it in allSymbols }.toSet().joinToString("")
            val hasSymbols = detectedSymbols.isNotEmpty()

            val formattedDamage = when {
                showFormatted -> format(event.damage)
                useCommas -> commaFormatter.format(event.damage)
                else -> event.damage.toString()
            }

            val newName = when {
                hasSymbols && enableRainbow -> addRandomColorCodes(detectedSymbols + formattedDamage + detectedSymbols)
                hasSymbols -> "${criticalDamageColor.code}${detectedSymbols}$formattedDamage${detectedSymbols}"
                else -> "${normalDamageColor.code}$formattedDamage"
            }

            nameObj.`object` = addColor(newName)
        }
    }

    private fun detectDamageType(originalName: String, cleanName: String): DamageType {
        return when {
            cleanName.contains("✧") -> DamageType.CRIT
            cleanName.contains("✯") -> DamageType.OVERLOAD
            originalName.contains("§6") -> DamageType.FIRE
            else -> DamageType.NORMAL
        }
    }

    private fun addRandomColorCodes(inputString: String): String {
        val cleanString = inputString.removeFormatting()
        if (cleanString.isEmpty()) return inputString

        val baseColorCodes = arrayOf("§6", "§c", "§e", "§f", "§a", "§b", "§d", "§9")
        val activeColors = enabledColors.map { baseColorCodes[it] }.toTypedArray()
        if (activeColors.isEmpty()) return inputString

        return buildString(cleanString.length * 6) {
            var lastColorIndex = -1

            for (char in cleanString) {
                var colorIndex: Int
                do colorIndex = activeColors.indices.random() while (colorIndex == lastColorIndex && activeColors.size > 1)

                append(activeColors[colorIndex])
                append(char)
                append("§r")
                lastColorIndex = colorIndex
            }
        }
    }
}