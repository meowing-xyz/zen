package meowing.zen.config.ui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.createBlock
import java.awt.Color
import kotlin.math.min

class Dropdown(
    private val options: List<String> = emptyList(),
    initialSelected: Int = 0,
    private val onChange: ((Int) -> Unit)? = null
) : UIContainer() {

    companion object {
        private var dropdownContainer: UIContainer? = null
        var isDropdownOpen = false

        fun closeDropdown() {
            if (!isDropdownOpen) return
            dropdownContainer?.parent?.removeChild(dropdownContainer!!)
            dropdownContainer = null
            isDropdownOpen = false
        }
    }

    private var selectedIndex: Int = min(initialSelected, options.size - 1)
    private val theme = ConfigTheme()
    private var selectedText: UIWrappedText
    private var container: UIComponent

    init {
        container = createBlock(3f).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        }.setColor(theme.element) childOf this

        selectedText = (UIWrappedText(options.getOrNull(selectedIndex) ?: "", centered = true).constrain {
            x = 5.percent()
            y = CenterConstraint()
            width = mc.fontRendererObj.getStringWidth(options.getOrNull(selectedIndex) ?: "").pixels()
        }.setColor(Color(200, 230, 235, 255)) childOf container) as UIWrappedText

        container.onMouseClick { event ->
            event.stopPropagation()
            toggleDropdown()
        }

        container.onMouseEnter {
            container.setColor(Color(25, 50, 60, 180))
        }

        container.onMouseLeave {
            if (!isDropdownOpen) {
                container.setColor(theme.element)
            }
        }
    }

    private fun toggleDropdown() {
        if (isDropdownOpen) closeDropdown() else openDropdown()
    }

    private fun openDropdown() {
        if (isDropdownOpen) return

        val window = Window.of(this)
        val maxHeight = min(options.size * 30, 200)

        dropdownContainer = UIContainer().constrain {
            x = this@Dropdown.getLeft().pixels()
            y = (this@Dropdown.getBottom() + 5f).pixels()
            width = this@Dropdown.getWidth().pixels()
            height = maxHeight.pixels()
        }.childOf(window)

        val background = UIBlock(theme.popup).constrain {
            width = 100.percent()
            height = 100.percent()
        }.childOf(dropdownContainer!!).effect(OutlineEffect(theme.border, 1f))

        val scrollComponent = ScrollComponent().constrain {
            x = 2.pixels()
            y = 2.pixels()
            width = 100.percent() - 4.pixels()
            height = 100.percent() - 4.pixels()
        } childOf background

        createOptions(scrollComponent)
        isDropdownOpen = true
        container.setColor(theme.accent)

        dropdownContainer!!.onMouseClick { event ->
            event.stopPropagation()
        }

        scrollComponent.onMouseScroll { event ->
            event.stopPropagation()
        }
    }

    private fun createOptions(parent: UIComponent) {
        options.forEachIndexed { index, option ->
            val optionComponent = createBlock(2f).constrain {
                x = 0.percent()
                y = (index * 30).pixels()
                width = 100.percent()
                height = 28.pixels()
            }.setColor(if (index == selectedIndex) theme.accent else Color(0, 0, 0, 0)) childOf parent

            optionComponent.onMouseClick { event ->
                event.stopPropagation()
                selectOption(index)
            }

            optionComponent.onMouseEnter {
                if (index != selectedIndex) {
                    optionComponent.setColor(Color(theme.border.red, theme.border.green, theme.border.blue, 100))
                }
            }

            optionComponent.onMouseLeave {
                if (index != selectedIndex) {
                    optionComponent.setColor(Color(0, 0, 0, 0))
                }
            }

            UIText(option).constrain {
                x = 8.pixels()
                y = CenterConstraint()
            }.setColor(Color(200, 230, 235, 255)) childOf optionComponent
        }
    }

    private fun selectOption(index: Int) {
        selectedIndex = index
        selectedText.setText(options[index])
        onChange?.invoke(index)
        closeDropdown()
        container.setColor(theme.element)
    }
}