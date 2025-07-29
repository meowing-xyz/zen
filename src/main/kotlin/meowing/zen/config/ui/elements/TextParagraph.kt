package meowing.zen.config.ui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import java.awt.Color

class TextParagraph(
    private val text: String,
    private val centered: Boolean = true,
    private val textColor: Color = Color(100, 245, 255, 255)
) : UIContainer() {
    private lateinit var textComponent: UIComponent

    init {
        createComponent()
    }

    private fun createComponent() {
        textComponent = UIWrappedText(text, centered = centered).constrain {
            x = 2.percent()
            y = CenterConstraint()
            width = 96.percent()
            textScale = 1.0.pixels()
        }.setColor(textColor) childOf this
    }
}