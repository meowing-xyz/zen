package meowing.zen.config.ui.elements

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import meowing.zen.utils.Utils.createBlock
import java.awt.Color

class TextInput(
    initialValue: String = "",
    placeholder: String = "",
    private val onChange: ((String) -> Unit)? = null
) : UIContainer() {
    var text: String = initialValue
    private val input: UITextInput
    private val placeholderText: UIText?
    private var onInputCallback: ((String) -> Unit)? = null

    init {
        val container = createBlock(3f).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        }.setColor(Color(18, 24, 28, 255)) childOf this

        input = (UITextInput(text).constrain {
            x = 8.pixels()
            y = CenterConstraint()
            width = 100.percent() - 16.pixels()
            height = 10.pixels()
        }.setColor(Color(170, 230, 240, 255)) childOf container) as UITextInput

        placeholderText = (if (placeholder.isNotEmpty()) {
            UIText(placeholder).constrain {
                x = 8.pixels()
                y = CenterConstraint()
            }.setColor(Color(70, 120, 140, 255)) childOf container
        } else null) as UIText?

        updatePlaceholderVisibility()
        setupEventHandlers()
    }

    private fun setupEventHandlers() {
        onMouseClick {
            input.setText(text)
            input.grabWindowFocus()
        }

        input.onKeyType { _, _ ->
            text = input.getText()
            updatePlaceholderVisibility()
            onChange?.invoke(text)
            onInputCallback?.invoke(text)
        }

        input.onFocusLost {
            text = input.getText()
            onChange?.invoke(text)
        }
    }

    private fun updatePlaceholderVisibility() {
        placeholderText?.let { placeholder ->
            if (text.isEmpty()) placeholder.unhide(true)
            else placeholder.hide(true)
        }
    }

    fun onKeyInput(callback: (String) -> Unit): TextInput {
        onInputCallback = callback
        return this
    }

    fun grabFocus() {
        input.grabWindowFocus()
    }
}