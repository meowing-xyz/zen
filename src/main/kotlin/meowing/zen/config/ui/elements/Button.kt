package meowing.zen.config.ui.elements

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import meowing.zen.utils.Utils.createBlock
import java.awt.Color

class Button(
    text: String,
    private val onClick: (() -> Unit)? = null
) : UIContainer() {
    private val normalBg = Color(18, 24, 28, 255)
    private val hoverBg = Color(25, 35, 40, 255)
    private val pressedBg = Color(40, 80, 90, 255)
    private val textColor = Color(170, 230, 240, 255)

    init {
        val container = createBlock(3f).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        }.setColor(normalBg) childOf this

        UIText(text).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.9.pixels()
        }.setColor(textColor) childOf container

        container.onMouseEnter {
            animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, hoverBg.toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, normalBg.toConstraint()) }
        }.onMouseClick {
            onClick?.invoke()
            animate {
                setColorAnimation(Animations.OUT_EXP, 0.1f, pressedBg.toConstraint())
                onComplete {
                    animate { setColorAnimation(Animations.OUT_QUAD, 0.2f, normalBg.toConstraint()) }
                }
            }
        }
    }
}