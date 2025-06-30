package meowing.zen.config.ui.core

import gg.essential.elementa.UIComponent
import meowing.zen.config.ui.ConfigData
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.elements.Button
import meowing.zen.config.ui.elements.Colorpicker
import meowing.zen.config.ui.elements.Dropdown
import meowing.zen.config.ui.elements.Slider
import meowing.zen.config.ui.elements.Switch
import meowing.zen.config.ui.elements.TextInput
import meowing.zen.config.ui.elements.TextParagraph
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import java.awt.Color

class ElementFactory(private val theme: ConfigTheme) {
    fun createButton(element: ConfigElement, config: ConfigData, ui: ConfigUI): UIComponent {
        val buttonType = element.type as ElementType.Button
        return Button(buttonType.text) {
            buttonType.onClick(config, ui)
        }
    }

    fun createSwitch(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val switchType = element.type as ElementType.Switch
        val currentValue = config[element.configKey] as? Boolean ?: switchType.default
        return Switch(currentValue, onUpdate)
    }

    fun createSlider(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val sliderType = element.type as ElementType.Slider
        val currentValue = when (val configVal = config[element.configKey]) {
            is Int -> configVal.toDouble()
            is Double -> configVal
            else -> sliderType.default
        }
        return Slider(sliderType.min, sliderType.max, currentValue, sliderType.showDouble,onUpdate)
    }

    fun createDropdown(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val dropdownType = element.type as ElementType.Dropdown
        val currentValue = when (val configVal = config[element.configKey]) {
            is Int -> configVal
            is Double -> configVal.toInt()
            else -> dropdownType.default
        }
        return Dropdown(dropdownType.options, currentValue, onUpdate)
    }

    fun createTextInput(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val textType = element.type as ElementType.TextInput
        val currentValue = config[element.configKey] as? String ?: textType.default
        return TextInput(currentValue, textType.placeholder, onUpdate)
    }

    fun createTextParagraph(element: ConfigElement): UIComponent {
        val textType = element.type as ElementType.TextParagraph
        return TextParagraph(textType.text, true, theme.accent)
    }

    fun createColorPicker(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val colorType = element.type as ElementType.ColorPicker
        val currentValue = when (val configVal = config[element.configKey]) {
            is Color -> configVal
            is List<*> -> {
                val values = configVal.mapNotNull { (it as? Number)?.toInt() }
                if (values.size >= 4) Color(values[0], values[1], values[2], values[3])
                else colorType.default
            }
            else -> colorType.default
        }
        return Colorpicker(currentValue, onUpdate)
    }
}