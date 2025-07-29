package meowing.zen.config.ui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import meowing.zen.utils.Utils.createBlock
import java.awt.Color

class Switch(
    private var isOn: Boolean = false,
    roundness: Float = 6f,
    private val handleWidth: Float = 25f,
    private val onChange: ((Boolean) -> Unit)? = null
) : UIContainer() {
    private val onColor = Color(100, 245, 255, 255)
    private val offColor = Color(35, 40, 45, 255)
    private val bgColor = Color(18, 22, 26, 255)

    private val handle: UIComponent

    init {
        val bg = createBlock(roundness).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        }.setColor(bgColor) childOf this

        handle = createBlock(roundness).constrain {
            x = if (isOn) (100 - handleWidth - 5).percent() else 5.percent()
            y = CenterConstraint()
            width = handleWidth.percent()
            height = 80.percent()
        }.setColor(if (isOn) onColor else offColor) childOf bg

        onMouseClick {
            toggle()
        }
    }

    fun setValue(value: Boolean, skipAnimation: Boolean = false) {
        isOn = value

        if (skipAnimation) {
            handle.constrain {
                x = if (isOn) (100 - handleWidth - 5).percent() else 5.percent()
            }
            handle.setColor(if (isOn) onColor else offColor)
        } else {
            handle.animate {
                setXAnimation(Animations.OUT_EXP, 0.5f, if (isOn) (100 - handleWidth - 5).percent() else 5.percent())
                setColorAnimation(Animations.OUT_EXP, 0.5f, (if (isOn) onColor else offColor).toConstraint())
            }
        }
    }

    fun toggle() {
        setValue(!isOn)
        onChange?.invoke(isOn)
    }

    fun getValue(): Boolean = isOn
}