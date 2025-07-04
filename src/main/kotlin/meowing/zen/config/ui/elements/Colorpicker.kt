package meowing.zen.config.ui.elements

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import java.awt.Color

class Colorpicker(
    initialValue: Color = Color(255, 255, 255, 255),
    private val onChange: ((Color) -> Unit)? = null
) : UIContainer() {

    private var value: Color = initialValue
    private val colorPreview: UIRoundedRectangle

    init {
        constrain {
            width = 400.pixels()
            height = 30.pixels()
        }

        colorPreview = (UIRoundedRectangle(3f).constrain {
            x = 0.pixels()
            y = CenterConstraint()
            width = 30.pixels()
            height = 30.pixels()
        }.setColor(value) childOf this) as UIRoundedRectangle

        createSlider("R", Color.RED, value.red.toDouble()) { r ->
            value = Color(r.toInt(), value.green, value.blue, value.alpha)
            colorPreview.setColor(value)
            onChange?.invoke(value)
        }

        createSlider("G", Color.GREEN, value.green.toDouble()) { g ->
            value = Color(value.red, g.toInt(), value.blue, value.alpha)
            colorPreview.setColor(value)
            onChange?.invoke(value)
        }

        createSlider("B", Color.BLUE, value.blue.toDouble()) { b ->
            value = Color(value.red, value.green, b.toInt(), value.alpha)
            colorPreview.setColor(value)
            onChange?.invoke(value)
        }

        createSlider("A", Color.WHITE, value.alpha.toDouble()) { a ->
            value = Color(value.red, value.green, value.blue, a.toInt())
            colorPreview.setColor(value)
            onChange?.invoke(value)
        }
    }

    private fun createSlider(label: String, color: Color, initial: Double, update: (Double) -> Unit) {
        UIText(label).constrain {
            x = SiblingConstraint(10f)
            y = CenterConstraint()
        }.setColor(color) childOf this

        Slider(0.0, 255.0, initial, false, update).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 80.pixels()
            height = 20.pixels()
        } childOf this
    }
}