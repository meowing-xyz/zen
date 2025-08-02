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
        return ButtonElement(type.text) { type.onClick(config, ui) }
    }

    fun createSwitch(element: ConfigElement, config: ConfigData, roundness: Float = 3f, handleWidth: Float = 25f, onUpdate: (Any) -> Unit): UIComponent {
        val type = element.type as ElementType.Switch
        return SwitchElement(config[element.configKey] as? Boolean ?: type.default, roundness, handleWidth, onChange = onUpdate)
    }

    fun createSlider(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val type = element.type as ElementType.Slider
        val value = config[element.configKey] as? Double ?: type.default
        return SliderElement(type.min, type.max, value, type.showDouble, onUpdate)
    }

    fun createDropdown(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val type = element.type as ElementType.Dropdown
        val index = when (val v = config[element.configKey]) {
            is Int -> v
            is Double -> v.toInt()
            else -> type.default
        }
        return DropdownElement(type.options, index, onUpdate)
    }

    fun createTextInput(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val type = element.type as ElementType.TextInput
        return TextInputElement(config[element.configKey] as? String ?: type.default, type.placeholder, onUpdate)
    }

    fun createTextParagraph(element: ConfigElement): UIComponent {
        val type = element.type as ElementType.TextParagraph
        return TextParagraphElement(type.text, true, theme.accent)
    }

    fun createColorPicker(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val type = element.type as ElementType.ColorPicker
        val value = config[element.configKey]?.let { configValue ->
            when (configValue) {
                is Color -> configValue
                is Map<*, *> -> {
                    if (configValue.containsKey("r")) {
                        val r = (configValue["r"] as? Number)?.toInt() ?: 255
                        val g = (configValue["g"] as? Number)?.toInt() ?: 255
                        val b = (configValue["b"] as? Number)?.toInt() ?: 255
                        val a = (configValue["a"] as? Number)?.toInt() ?: 255
                        Color(r, g, b, a)
                    } else {
                        configValue.toColorFromMap()
                    }
                }
                // Only in temporarily to ensure backwards compat
                is List<*> -> configValue.toColorFromList()
                is Number -> Color(configValue.toInt(), true)
                else -> null
            }
        } ?: type.default

        return ColorPickerElement(value) { color ->
            onUpdate(mapOf(
                "r" to color.red,
                "g" to color.green,
                "b" to color.blue,
                "a" to color.alpha
            ))
        }
    }

    fun createKeybind(element: ConfigElement, config: ConfigData, onUpdate: (Any) -> Unit): UIComponent {
        val type = element.type as ElementType.Keybind
        val confKey = config[element.configKey]
        val keyCode = when (confKey) {
            is Int -> confKey
            is Double -> confKey.toInt()
            else -> type.default
        }
        return KeybindElement(keyCode, onUpdate, theme)
    }

    fun updateSwitchValue(switchComponent: UIComponent, newValue: Boolean) {
        if (switchComponent is SwitchElement) {
            switchComponent.setValue(newValue)
        }
    }
}