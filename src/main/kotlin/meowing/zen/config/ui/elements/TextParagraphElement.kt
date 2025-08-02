package meowing.zen.config.ui.elements

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import java.awt.Color

class TextParagraphElement(
    text: String,
    private val centered: Boolean = false,
    textColor: Color = Color(170, 230, 240, 255)
) : UIContainer() {
    init {
        UIWrappedText(text, centered = centered).constrain {
            x = CenterConstraint()
            y = CenterConstraint() - 6.pixels
            width = 100.percent()
            textScale = 0.8.pixels()
        }.setColor(textColor) childOf this
    }
}