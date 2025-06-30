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
    private val rSlider: Slider
    private val gSlider: Slider
    private val bSlider: Slider
    private val aSlider: Slider

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

        UIText("R").constrain {
            x = SiblingConstraint(10f)
            y = CenterConstraint()
        }.setColor(Color.RED) childOf this

        rSlider = Slider(
            min = 0.0,
            max = 255.0,
            initialValue = value.red.toDouble(),
            showDouble = false,
            onChange = { updateColor() }
        ).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 80.pixels()
            height = 20.pixels()
        } childOf this

        UIText("G").constrain {
            x = SiblingConstraint(10f)
            y = CenterConstraint()
        }.setColor(Color.GREEN) childOf this

        gSlider = Slider(
            min = 0.0,
            max = 255.0,
            initialValue = value.green.toDouble(),
            showDouble = false,
            onChange = { updateColor() }
        ).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 80.pixels()
            height = 20.pixels()
        } childOf this

        UIText("B").constrain {
            x = SiblingConstraint(10f)
            y = CenterConstraint()
        }.setColor(Color.BLUE) childOf this

        bSlider = Slider(
            min = 0.0,
            max = 255.0,
            initialValue = value.blue.toDouble(),
            showDouble = false,
            onChange = { updateColor() }
        ).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 80.pixels()
            height = 20.pixels()
        } childOf this

        UIText("A").constrain {
            x = SiblingConstraint(10f)
            y = CenterConstraint()
        }.setColor(Color.WHITE) childOf this

        aSlider = Slider(
            min = 0.0,
            max = 255.0,
            initialValue = value.alpha.toDouble(),
            showDouble = false,
            onChange = { updateColor() }
        ).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 80.pixels()
            height = 20.pixels()
        } childOf this
    }

    private fun updateColor() {
        val newValue = Color(
            rSlider.getValue().toInt(),
            gSlider.getValue().toInt(),
            bSlider.getValue().toInt(),
            aSlider.getValue().toInt()
        )

        if (newValue != value) {
            value = newValue
            colorPreview.setColor(value)
            onChange?.invoke(value)
        }
    }

    fun getValue(): Color = value

    fun setValue(newValue: Color) {
        val clampedValue = Color(newValue.red, newValue.green, newValue.blue, newValue.alpha)
        if (clampedValue != value) {
            value = clampedValue

            rSlider.setValue(value.red.toDouble())
            gSlider.setValue(value.green.toDouble())
            bSlider.setValue(value.blue.toDouble())
            aSlider.setValue(value.alpha.toDouble())

            colorPreview.setColor(value)
            onChange?.invoke(value)
        }
    }
}