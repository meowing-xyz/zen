package meowing.zen.config.ui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.utils.withAlpha
import meowing.zen.utils.Utils.createBlock
import java.awt.Color

enum class MCColorCode(val code: String, val color: Color, val displayName: String) {
    BLACK("§0", Color(0, 0, 0), "Black"),
    DARK_BLUE("§1", Color(0, 0, 170), "Dark Blue"),
    DARK_GREEN("§2", Color(0, 170, 0), "Dark Green"),
    DARK_AQUA("§3", Color(0, 170, 170), "Dark Aqua"),
    DARK_RED("§4", Color(170, 0, 0), "Dark Red"),
    DARK_PURPLE("§5", Color(170, 0, 170), "Dark Purple"),
    GOLD("§6", Color(255, 170, 0), "Gold"),
    GRAY("§7", Color(170, 170, 170), "Gray"),
    DARK_GRAY("§8", Color(85, 85, 85), "Dark Gray"),
    BLUE("§9", Color(85, 85, 255), "Blue"),
    GREEN("§a", Color(85, 255, 85), "Green"),
    AQUA("§b", Color(85, 255, 255), "Aqua"),
    RED("§c", Color(255, 85, 85), "Red"),
    LIGHT_PURPLE("§d", Color(255, 85, 255), "Light Purple"),
    YELLOW("§e", Color(255, 255, 85), "Yellow"),
    WHITE("§f", Color(255, 255, 255), "White")
}

class MCColorPickerElement(
    initialValue: MCColorCode = MCColorCode.WHITE,
    private val onChange: ((MCColorCode) -> Unit)? = null
) : UIContainer() {
    private var selectedColor: MCColorCode = initialValue
    private val colorGrid: UIContainer
    private val colorButtons = mutableMapOf<MCColorCode, UIComponent>()
    private val selectionBorders = mutableMapOf<MCColorCode, UIComponent>()

    init {
        colorGrid = UIContainer().constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = 100.percent()
            height = 18.pixels()
        } childOf this

        createColorButtons()
        updateSelection()
    }

    private fun createColorButtons() {
        val colors = MCColorCode.entries
        val totalColors = colors.size
        val spacing = 1f

        colors.forEachIndexed { index, colorCode ->
            val buttonWidthPercent = (100f - (totalColors - 1) * spacing) / totalColors
            val buttonWidth = buttonWidthPercent.percent()
            val xOffset = (index * (buttonWidthPercent + spacing)).percent()

            val borderContainer = UIContainer().constrain {
                x = xOffset
                y = 0.pixels()
                width = buttonWidth
                height = 100.percent()
            } childOf colorGrid

            val selectionBorder = createBlock(2f).constrain {
                x = (-1).pixels()
                y = (-1).pixels()
                width = 100.percent() + 2.pixels()
                height = 100.percent() + 2.pixels()
            }.setColor(Color(170, 230, 240, 255)) childOf borderContainer

            val button = createBlock(2f).constrain {
                x = 0.pixels()
                y = 0.pixels()
                width = 100.percent()
                height = 100.percent()
            }.setColor(colorCode.color) childOf borderContainer

            button.onMouseClick {
                selectedColor = colorCode
                updateSelection()
                onChange?.invoke(colorCode)
                button.setColor(colorCode.color)
            }

            button.onMouseEnter {
                if (selectedColor != colorCode) {
                    animate {
                        setColorAnimation(Animations.OUT_QUAD, 0.1f, colorCode.color.withAlpha(180).toConstraint())
                    }
                }
            }

            button.onMouseLeave {
                if (selectedColor != colorCode) {
                    animate {
                        setColorAnimation(Animations.OUT_QUAD, 0.1f, colorCode.color.toConstraint())
                    }
                }
            }

            colorButtons[colorCode] = button
            selectionBorders[colorCode] = selectionBorder
        }
    }

    private fun updateSelection() {
        selectionBorders.forEach { (colorCode, border) ->
            if (colorCode == selectedColor) border.unhide() else border.hide()
        }
    }

    fun setValue(colorCode: MCColorCode) {
        selectedColor = colorCode
        updateSelection()
    }

    fun getValue(): MCColorCode = selectedColor
}