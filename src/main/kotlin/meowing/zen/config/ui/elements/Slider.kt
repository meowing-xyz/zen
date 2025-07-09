package meowing.zen.config.ui.elements

import meowing.zen.Zen.Companion.mc
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class Slider(
    private val min: Double = 0.0,
    private val max: Double = 100.0,
    initialValue: Double = 50.0,
    private val showDouble: Boolean = false,
    private val onChange: ((Double) -> Unit)? = null
) : UIContainer() {
    private var value: Double = max(min, min(max, initialValue))
    private val sliderContainer: UIRoundedRectangle
    private val textContainer: UIRoundedRectangle
    private val progress: UIRoundedRectangle
    private val input: UITextInput

    init {
        sliderContainer = (UIRoundedRectangle(3f).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 70.percent()
            height = 100.percent()
        }.setColor(Color(18, 24, 28, 255)) childOf this) as UIRoundedRectangle

        textContainer = (UIRoundedRectangle(3f).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 25.percent()
            height = 100.percent()
        }.setColor(Color(18, 24, 28, 255)) childOf this) as UIRoundedRectangle

        val initialPercent = (value - min).toFloat() / (max - min).toFloat()
        progress = (UIRoundedRectangle(3f).constrain {
            x = 0.percent()
            y = 0.percent()
            width = (initialPercent * 100).percent()
            height = 100.percent()
        }.setColor(Color(100, 245, 255, 255)) childOf sliderContainer) as UIRoundedRectangle

        input = (UITextInput(formatDisplayValue(value)).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = mc.fontRendererObj.getStringWidth(formatDisplayValue(max)).pixels()
        }.setColor(Color(170, 230, 240, 255)) childOf textContainer) as UITextInput

        setupMouseHandlers()
        setupInputHandlers()
    }

    private fun formatDisplayValue(value: Double): String {
        return if (!showDouble && value == value.toInt().toDouble()) value.toInt().toString() else value.toString()
    }

    private fun setupMouseHandlers() {
        fun updateSliderPosition(mouseX: Float) {
            val clamped = mouseX.coerceIn(0f, sliderContainer.getWidth())
            val percent = clamped / sliderContainer.getWidth()
            updateSliderValue(percent)
        }

        sliderContainer.onMouseClick { event ->
            val withinBounds = event.relativeX in 0f..sliderContainer.getWidth() && event.relativeY in 0f..sliderContainer.getHeight()
            if (!withinBounds) return@onMouseClick
            updateSliderPosition(event.relativeX)
        }

        sliderContainer.onMouseDrag { x, y, _ ->
            val withinBounds = x in 0f..sliderContainer.getWidth() && y in -5f..(sliderContainer.getHeight() + 5f)
            if (!withinBounds) return@onMouseDrag
            updateSliderPosition(x)
        }
    }

    private fun setupInputHandlers() {
        input.onMouseClick {
            input.setText(formatDisplayValue(value))
            input.grabWindowFocus()
        }

        input.onKeyType { _, keyCode ->
            processInputValue()
        }

        input.onFocusLost {
            processInputValue()
        }
    }

    private fun processInputValue() {
        val inputText = input.getText().trim()

        if (inputText.isEmpty()) {
            setValue(min)
            return
        }

        val newValue = inputText.toDoubleOrNull()
        if (newValue != null) {
            val constrainedValue = max(0.0, min(max, newValue))
            setValue(constrainedValue)
            if (constrainedValue != newValue) input.setText(formatDisplayValue(constrainedValue))
        } else input.setText(formatDisplayValue(value))
    }

    private fun updateSliderValue(percent: Float) {
        val clampedPercent = percent.coerceIn(0f, 1f)
        val rawValue = min + (max - min) * clampedPercent
        val newValue = if (showDouble) (round(rawValue * 10) / 10.0) else round(rawValue)

        if (newValue != value) {
            value = newValue
            input.setText(formatDisplayValue(value))
            onChange?.invoke(value)
        }

        progress.animate {
            setWidthAnimation(Animations.OUT_EXP, 0.5f, (clampedPercent * 100).percent())
        }
    }

    fun getValue(): Double = value

    fun setValue(newValue: Double) {
        val clampedValue = max(min, min(max, newValue))
        if (clampedValue != value) {
            value = clampedValue
            input.setText(formatDisplayValue(value))
            val percent = (value - min).toFloat() / (max - min).toFloat()
            progress.animate {
                setWidthAnimation(Animations.OUT_EXP, 0.5f, (percent * 100).percent())
            }
            onChange?.invoke(value)
        }
    }
}