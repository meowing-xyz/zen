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

@Zen.Module
object DamageSplash : Feature("damagesplash") {
    private val allSymbols = setOf('✧', '✯', '⚔', '+', '❤', '♞', '☄', '✷', 'ﬗ')

    private val enableRainbow by ConfigDelegate<Boolean>("damagesplashrainbow")
    private val normalDamageColor by ConfigDelegate<MCColorCode>("damagesplashnormalcolor")
    private val criticalDamageColor by ConfigDelegate<MCColorCode>("damagesplashcriticalcolor")
    private val enabledColors by ConfigDelegate<Set<Int>>("damagesplashcolors")
    private val showFormatted by ConfigDelegate<Boolean>("damagesplashformatted")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Damage Splash", ConfigElement(
                "damagesplash",
                "Enhanced damage number display",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Damage Splash", "Display", ConfigElement(
                "damagesplashformatted",
                "Show formatted numbers",
                ElementType.Switch(true)
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
            val nameObj = event.packet.func_149027_c()?.find { it.objectType == 4 } ?: return@register
            val name = event.originalName.removeFormatting()

            val detectedSymbols = name.filter { it in allSymbols }.toSet().joinToString("")
            val hasSymbols = detectedSymbols.isNotEmpty()

            val formattedDamage = if (showFormatted) format(event.damage) else event.damage.toString()

            val newName = when {
                hasSymbols && enableRainbow -> addRandomColorCodes(detectedSymbols + formattedDamage + detectedSymbols)
                hasSymbols -> "${criticalDamageColor.code}${detectedSymbols}$formattedDamage${detectedSymbols}"
                else -> "${normalDamageColor.code}$formattedDamage"
            }

            nameObj.`object` = addColor(newName)
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