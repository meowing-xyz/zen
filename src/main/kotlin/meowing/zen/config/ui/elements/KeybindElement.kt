package meowing.zen.config.ui.elements

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import meowing.zen.config.ui.core.ConfigTheme
import meowing.zen.utils.Utils.createBlock
import org.lwjgl.input.Keyboard
import java.awt.Color

class KeybindElement(
    private var code: Int = 0,
    private val onKeyChange: ((Int) -> Unit)? = null,
    private val theme: ConfigTheme = ConfigTheme()
) : UIContainer() {
    private var listening = false
    private var keyDisplay: UIText

    init {
        val container = createBlock(6f).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        }.setColor(theme.element) childOf this

        keyDisplay = (UIText(getKeyName(code)).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        }.setColor(Color.WHITE) childOf container) as UIText

        container.onMouseClick {
            grabWindowFocus()
            listening = true
            keyDisplay.setText(".....")
            container.animate {
                setColorAnimation(Animations.OUT_EXP, 0.2f, theme.element.brighter().toConstraint())
            }
        }

        container.onKeyType { _, keycode ->
            if (listening) {
                if (keycode == Keyboard.KEY_ESCAPE) {
                    keyDisplay.setText("None").setColor(Color.WHITE)
                    code = 0
                    onKeyChange?.invoke(0)
                } else {
                    keyDisplay.setText(getKeyName(keycode)).setColor(Color.WHITE)
                    code = keycode
                    onKeyChange?.invoke(keycode)
                }
                listening = false
                container.animate {
                    setColorAnimation(Animations.OUT_EXP, 0.2f, theme.element.toConstraint())
                }
                loseFocus()
            }
        }
    }

    override fun keyType(typedChar: Char, keyCode: Int) {
        if (keyCode == 1 && listening) {
            keyDisplay.setText("None").setColor(Color.WHITE)
            code = 0
            onKeyChange?.invoke(0)
            listening = false
            loseFocus()
            return
        }
        super.keyType(typedChar, keyCode)
    }

    private fun getKeyName(keyCode: Int): String = when (keyCode) {
        0 -> "None"
        Keyboard.KEY_LSHIFT -> "LShift"
        Keyboard.KEY_RSHIFT -> "RShift"
        Keyboard.KEY_LCONTROL -> "LCtrl"
        Keyboard.KEY_RCONTROL -> "RCtrl"
        Keyboard.KEY_LMENU -> "LAlt"
        Keyboard.KEY_RMENU -> "RAlt"
        Keyboard.KEY_RETURN -> "Enter"
        Keyboard.KEY_ESCAPE -> "Escape"
        Keyboard.KEY_SPACE -> "Space"
        Keyboard.KEY_BACK -> "Backspace"
        Keyboard.KEY_TAB -> "Tab"
        Keyboard.KEY_CAPITAL -> "CapsLock"
        in Keyboard.KEY_F1..Keyboard.KEY_F12 -> "F${keyCode - Keyboard.KEY_F1 + 1}"
        else -> Keyboard.getKeyName(keyCode) ?: "Key $keyCode"
    }
}