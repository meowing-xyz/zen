package meowing.zen.config.ui.core

import gg.essential.elementa.UIComponent
import meowing.zen.config.ui.ConfigData
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.elements.*
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.utils.Utils.toColorFromList
import meowing.zen.utils.Utils.toColorFromMap
import java.awt.Color

class ElementFactory(private val theme: ConfigTheme) {
    fun createButton(element: ConfigElement, config: ConfigData, ui: ConfigUI): UIComponent {
        val type = element.type as ElementType.Button
        return Button(type.text) { type.onClick(config, ui) }
    }

    fun createSwitch(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val type = element.type as ElementType.Switch
        return Switch(config[element.configKey] as? Boolean ?: type.default, onUpdate)
    }

    fun createSlider(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val type = element.type as ElementType.Slider
        val value = config[element.configKey] as? Double ?: type.default
        return Slider(type.min, type.max, value, type.showDouble, onUpdate)
    }

    fun createDropdown(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val type = element.type as ElementType.Dropdown
        val index = when (val v = config[element.configKey]) {
            is Int -> v
            is Double -> v.toInt()
            else -> type.default
        }
        return Dropdown(type.options, index, onUpdate)
    }

    fun createTextInput(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val type = element.type as ElementType.TextInput
        return TextInput(config[element.configKey] as? String ?: type.default, type.placeholder, onUpdate)
    }

    fun createTextParagraph(element: ConfigElement): UIComponent {
        val type = element.type as ElementType.TextParagraph
        return TextParagraph(type.text, true, theme.accent)
    }

    fun createColorPicker(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val type = element.type as ElementType.ColorPicker
        val value = config[element.configKey]?.let { configValue ->
            when (configValue) {
                is Color -> configValue
                is Map<*, *> -> configValue.toColorFromMap()
                is List<*> -> configValue.toColorFromList()
                is Number -> Color(configValue.toInt(), true)
                else -> null
            }
        } ?: type.default

        return Colorpicker(value) { color ->
            onUpdate(mapOf(
                "value" to (color.red shl 16 or (color.green shl 8) or color.blue).toDouble(),
                "falpha" to color.alpha.toDouble() / 255.0
            ))
        }
    }
}