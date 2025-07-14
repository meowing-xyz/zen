package meowing.zen.config.ui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import meowing.zen.utils.Utils.createBlock
import java.awt.Color

class TextParagraph(
    private val text: String,
    private val centered: Boolean = true,
    private val textColor: Color = Color(100, 245, 255, 255)
) : UIComponent() {
    private lateinit var container: UIComponent
    private lateinit var textComponent: UIComponent

    init {
        createComponent()
    }

    private fun createComponent() {
        container = createBlock(0f).constrain {
            width = 100.percent()
            height = 100.percent()
        }.setColor(Color(0, 0, 0, 0)) childOf this

        textComponent = UIWrappedText(text, centered = centered).constrain {
            x = 2.percent()
            y = CenterConstraint()
            width = 96.percent()
            textScale = 1.0.pixels()
        }.setColor(textColor) childOf container
    }
}