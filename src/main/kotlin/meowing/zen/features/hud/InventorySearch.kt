package meowing.zen.features.hud

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.GuiEvent
import meowing.zen.features.Feature
import meowing.zen.ui.components.TextInputComponent
import meowing.zen.utils.FontUtils
import meowing.zen.utils.ItemUtils.lore
import meowing.zen.utils.NumberUtils.abbreviateNumber
import meowing.zen.utils.Render2D
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import java.util.Locale
import javax.script.ScriptEngineManager

@Zen.Module
object InventorySearch : Feature("inventorysearch") {
    private val searchLore by ConfigDelegate<Boolean>("inventorysearchlore")
    private val highlightType by ConfigDelegate<Int>("inventorysearchtype")
    private val color by ConfigDelegate<Color>("inventorysearchcolor")
    private val abbreviate by ConfigDelegate<Boolean>("inventorySearch.abbreviateNumbers")
    private val fontObj = FontUtils.getFontRenderer()

    private const val K_MULTIPLIER = 1_000.0
    private const val M_MULTIPLIER = 1_000_000.0
    private const val B_MULTIPLIER = 1_000_000_000.0

    private val sanitizeRegex = Regex("[^0-9+\\-*/().\\sxXKkMmBb]")
    private val multiplierRegex = Regex("([0-9]+(?:\\.[0-9]+)?)([kKmMbB])")
    private val xMultiplyRegex = Regex("[xX]")

    private val searchInput = TextInputComponent(
        placeholder = "Search...",
        x = 0,
        y = 0,
        width = 200,
        height = 20,
        radius = 3,
        accentColor = Color(170, 230, 240),
        hoverColor = Color(70, 120, 140)
    )

    private val scriptEngine = ScriptEngineManager(null).getEngineByName("JavaScript")
    private var mathResult: String? = null

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("HUD", "Inventory Search", ConfigElement(
                "inventorysearch",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("HUD", "Inventory Search", "Options", ConfigElement(
                "inventorysearchlore",
                "Search lore",
                ElementType.Switch(false)
            ))
            .addElement("HUD", "Inventory Search", "Options", ConfigElement(
                "inventorySearch.abbreviateNumbers",
                "Abbreviate result numbers",
                ElementType.Switch(false)
            ))
            .addElement("HUD", "Inventory Search", "Options", ConfigElement(
                "inventorysearchcolor",
                "Highlight color",
                ElementType.ColorPicker(Color(0, 127, 127, 127))
            ))
            .addElement("HUD", "Inventory Search", "Options", ConfigElement(
                "inventorysearchtype",
                "Highlight type",
                ElementType.Dropdown(listOf("Slot", "Border"), 0)
            ))
    }

    private fun calculateMath(input: String): String? {
        return try {
            val sanitized = input.replace(sanitizeRegex, "")
            if (sanitized.isBlank() || sanitized.trim() != input.trim()) return null

            val processed = sanitized
                .replace(multiplierRegex) { match ->
                    val number = match.groupValues[1].toDouble()
                    val multiplier = when (match.groupValues[2].lowercase()) {
                        "k" -> K_MULTIPLIER
                        "m" -> M_MULTIPLIER
                        "b" -> B_MULTIPLIER
                        else -> 1.0
                    }
                    (number * multiplier).toString()
                }
                .replace(xMultiplyRegex, "*")

            scriptEngine?.eval(processed)?.toString()?.toDoubleOrNull()?.let { result ->
                if (abbreviate) result.abbreviateNumber()
                else "%.1f".format(Locale.US, result).removeSuffix(".0")
            }
        } catch (_: Exception) {
            null
        }
    }

    override fun initialize() {
        register<GuiEvent.BackgroundDraw> { event ->
            if (event.gui is GuiContainer) {
                searchInput.run {
                    val guiScale = if (mc.gameSettings.guiScale == 0) {
                        sr.scaleFactor
                    } else {
                        mc.gameSettings.guiScale
                    }
                    val sf = guiScale.toDouble() / sr.scaleFactor

                    val screenWidth = sr.scaledWidth / sf
                    val screenHeight = sr.scaledHeight / sf
                    x = (screenWidth - width.toDouble()) / 2
                    y = (screenHeight * 0.95 - height.toInt() / 2)

                    GlStateManager.pushMatrix()
                    GlStateManager.scale(sf, sf, sf)
                    GlStateManager.translate(0f, 0f, 300f)
                    draw(mouseX, mouseY)

                    mathResult?.let { result ->
                        if (value.isNotEmpty()) {
                            val textEndX = (x.toInt() + textPadding.toInt() - scrollOffset.toInt() + fontObj.getStringWidth(value)).toFloat()
                            val textY = (y.toInt() + (height.toInt() - fontObj.FONT_HEIGHT - 0.5) / 2).toFloat()
                            Render2D.renderString(" = $result", textEndX, textY, 1f, 0x55FF55)
                        }
                    }

                    GlStateManager.popMatrix()
                }
            }
        }

        register<GuiEvent.Click> { event ->
            if (event.gui is GuiContainer) {
                val button = Mouse.getEventButton()
                searchInput.mouseClicked(mouseX.toDouble(), mouseY.toDouble(), button)

                if (button == 1 &&
                    mouseX.toDouble() in searchInput.x.toDouble()..(searchInput.x.toDouble() + searchInput.width.toDouble()) &&
                    mouseY.toDouble() in searchInput.y.toDouble()..(searchInput.y.toDouble() + searchInput.height.toDouble())
                ) {
                    searchInput.value = ""
                    searchInput.focused = true
                }

                mathResult = if (searchInput.value.isNotEmpty()) calculateMath(searchInput.value) else null
            }
        }

        register<GuiEvent.Key> { event ->
            if (event.gui is GuiContainer) {
                val typedChar = Keyboard.getEventCharacter()
                val keyCode = Keyboard.getEventKey()

                if (Keyboard.getEventKeyState() && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && keyCode == Keyboard.KEY_F) {
                    searchInput.focused = !searchInput.focused
                    event.cancel()
                    return@register
                }

                if (searchInput.keyTyped(typedChar, keyCode)) {
                    mathResult = if (searchInput.value.isNotEmpty()) calculateMath(searchInput.value) else null
                    event.cancel()
                }
            }
        }

        register<GuiEvent.Slot.RenderPre> { event ->
            val text = searchInput.value.lowercase().removeFormatting().takeIf { it.isNotBlank() } ?: return@register
            val item = event.slot.stack ?: return@register
            val itemName = item.displayName.removeFormatting().trim().lowercase()
            val searchableText =
                if (searchLore) {
                    (item.lore.map { it.removeFormatting().lowercase() } + itemName).joinToString(" ")
                } else {
                    itemName
                }

            if (!searchableText.contains(text)) return@register

            val highlightColor = color.rgb
            val x = event.slot.xDisplayPosition
            val y = event.slot.yDisplayPosition

            when (highlightType) {
                0 -> Gui.drawRect(x, y, x + 16, y + 16, highlightColor)
                1 -> {
                    Gui.drawRect(x, y, x + 16, y + 1, highlightColor)
                    Gui.drawRect(x, y, x + 1, y + 16, highlightColor)
                    Gui.drawRect(x + 15, y, x + 16, y + 16, highlightColor)
                    Gui.drawRect(x, y + 15, x + 16, y + 16, highlightColor)
                }
            }
        }
    }
}