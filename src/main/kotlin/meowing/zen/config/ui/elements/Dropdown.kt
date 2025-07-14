package meowing.zen.config.ui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import meowing.zen.utils.Utils.createBlock
import java.awt.Color
import kotlin.math.min

class Dropdown(
    private val options: List<String> = emptyList(),
    initialSelected: Int = 0,
    private val onChange: ((Int) -> Unit)? = null
) : UIComponent() {

    private var selectedIndex: Int = min(initialSelected, options.size - 1)
    private var isOpen = false

    private val backgroundColor = Color(18, 24, 28, 255)
    private val selectedColor = Color(70, 180, 200, 255)
    private val hoverColor = Color(25, 50, 60, 180)
    private val textColor = Color(200, 230, 235, 255)

    private lateinit var selectedText: UIText
    private lateinit var dropdownBg: UIComponent
    private lateinit var scrollComponent: ScrollComponent
    private val optionComponents = mutableListOf<UIComponent>()

    init {
        setColor(backgroundColor)
        createDropdown()
    }

    private fun createDropdown() {
        selectedText = (UIText(options.getOrNull(selectedIndex) ?: "").constrain {
            x = 5.percent()
            y = CenterConstraint()
        }.setColor(textColor) childOf this) as UIText

        dropdownBg = createBlock(3f).constrain {
            x = 0.percent()
            y = 100.percent()
            width = 100.percent()
            height = min(options.size * 25, 150).pixels()
        }.setColor(backgroundColor) childOf this

        scrollComponent = ScrollComponent().constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 100.percent()
        } childOf dropdownBg

        createOptions()
        dropdownBg.hide()

        onMouseClick { event ->
            event.stopPropagation()
            isOpen = !isOpen
            updateDropdownState()
        }

        scrollComponent.onMouseScroll { event ->
            event.stopPropagation()
        }
    }

    private fun createOptions() {
        optionComponents.clear()
        options.forEachIndexed { index, option ->
            val optionComponent = createBlock(3f).constrain {
                x = 2.percent()
                y = (index * 25).pixels()
                width = 96.percent()
                height = 20.pixels()
            }.setColor(if (index == selectedIndex) selectedColor else backgroundColor) childOf scrollComponent

            optionComponent.onMouseClick { event ->
                event.stopPropagation()
                selectOption(index)
            }

            optionComponent.onMouseEnter {
                if (index != selectedIndex) {
                    optionComponent.setColor(hoverColor)
                }
            }

            optionComponent.onMouseLeave {
                if (index != selectedIndex) {
                    optionComponent.setColor(backgroundColor)
                }
            }

            UIText(option).constrain {
                x = 5.percent()
                y = CenterConstraint()
            }.setColor(textColor) childOf optionComponent

            optionComponents.add(optionComponent)
        }
    }

    private fun updateDropdownState() {
        if (isOpen) {
            dropdownBg.unhide(true)
            dropdownBg.isFloating = true
        } else {
            dropdownBg.hide(true)
            dropdownBg.isFloating = false
        }
    }

    private fun selectOption(index: Int) {
        optionComponents.forEachIndexed { i, component ->
            component.setColor(if (i == index) selectedColor else backgroundColor)
        }

        selectedIndex = index
        selectedText.setText(options[index])
        onChange?.invoke(index)
        isOpen = false
        updateDropdownState()
    }
}