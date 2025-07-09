package meowing.zen.config.ui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UMatrixStack
import java.awt.Color
import kotlin.math.roundToInt

class ConfigTheme {
    val panel = Color(4, 6, 8, 255)
    val element = Color(12, 16, 20, 255)
    val accent = Color(100, 245, 255, 255)
    val popup = Color(6, 10, 14, 255)
    val border = Color(60, 80, 100, 255)
}

class Colorpicker(
    initialValue: Color = Color.WHITE,
    private val onChange: ((Color) -> Unit)? = null
) : UIContainer() {
    companion object {
        private var pickerContainer: UIContainer? = null
        var isPickerOpen = false

        fun closePicker() {
            if (!isPickerOpen) return
            pickerContainer?.parent?.removeChild(pickerContainer!!)
            pickerContainer = null
            isPickerOpen = false
        }
    }

    private var value: Color = initialValue
    private val colorPreview: UIRoundedRectangle
    private val theme = ConfigTheme()

    init {
        colorPreview = UIRoundedRectangle(3f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent()
            height = 100.percent()
        }.setColor(value).childOf(this) as UIRoundedRectangle

        colorPreview.onMouseClick {
            togglePicker()
        }
    }

    private fun togglePicker() {
        if (isPickerOpen) closePicker() else openPicker()
    }

    private fun openPicker() {
        if (isPickerOpen) return

        val window = Window.of(this)

        pickerContainer = UIContainer().constrain {
            x = (this@Colorpicker.getRight() + 5f).pixels()
            y = this@Colorpicker.getTop().pixels()
            width = 130.pixels()
            height = 100.pixels()
        }.childOf(window)

        val background = UIBlock(theme.popup).constrain {
            width = 100.percent()
            height = 100.percent()
        }.childOf(pickerContainer!!).effect(OutlineEffect(theme.border, 1f))

        ColorPicker(value).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 90.percent()
            height = 80.pixels()
        }.childOf(background).onValueChange { color ->
            value = color
            colorPreview.setColor(color)
            onChange?.invoke(color)
        }

        isPickerOpen = true
    }
}

class ColorPicker(val initialColor: Color) : UIContainer() {
    private var currentColor = initialColor
    private var currentHue: Float
    private var currentSaturation: Float
    private var currentBrightness: Float
    private var currentAlpha = initialColor.alpha / 255f
    private var onValueChange: (Color) -> Unit = {}
    private var draggingHue = false
    private var draggingPicker = false
    private var draggingAlpha = false
    private var isChroma = false
    private val theme = ConfigTheme()

    init {
        val hsb = Color.RGBtoHSB(initialColor.red, initialColor.green, initialColor.blue, null)
        currentHue = hsb[0]
        currentSaturation = hsb[1]
        currentBrightness = hsb[2]

        setupUI()
    }

    private fun setupUI() {
        val pickerBox = UIBlock().constrain {
            width = 80.pixels()
            height = 100.percent()
            color = theme.border.toConstraint()
        }.childOf(this)

        val pickerIndicator = UIContainer().constrain {
            x = (RelativeConstraint(currentSaturation) - 3.5f.pixels()).coerceIn(2.pixels(), 2.pixels(alignOpposite = true))
            y = (RelativeConstraint(1f - currentBrightness) - 3.5f.pixels()).coerceIn(2.pixels(), 2.pixels(alignOpposite = true))
            width = 3.pixels()
            height = 3.pixels()
        }.effect(OutlineEffect(theme.accent, 1f))

        pickerBox.addChild(createCustomRenderer { matrixStack, component ->
            drawColorPicker(matrixStack, component)
        }.constrain {
            x = 1.pixels()
            y = (-0.5).pixels()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        } as UIComponent).addChild(pickerIndicator)

        pickerBox.onMouseClick { event ->
            isChroma = false
            draggingPicker = true
            currentSaturation = event.relativeX / pickerBox.getWidth()
            currentBrightness = 1f - (event.relativeY / pickerBox.getHeight())
            updatePickerIndicator(pickerIndicator)
        }.onMouseDrag { mouseX, mouseY, _ ->
            if (!draggingPicker) return@onMouseDrag
            currentSaturation = (mouseX / pickerBox.getWidth()).coerceIn(0f..1f)
            currentBrightness = 1f - ((mouseY / pickerBox.getHeight()).coerceIn(0f..1f))
            updatePickerIndicator(pickerIndicator)
        }.onMouseRelease { draggingPicker = false }

        val hueLine = UIBlock().constrain {
            x = SiblingConstraint(5f)
            width = 14.pixels()
            height = 100.percent()
            color = theme.border.toConstraint()
        }.childOf(this)

        val hueIndicator = UIText("◄").constrain {
            x = (-4).pixels(alignOpposite = true)
            y = RelativeConstraint(currentHue) - 5.pixels()
            color = theme.accent.toConstraint()
        }

        hueLine.addChild(createCustomRenderer { matrixStack, component ->
            drawHueLine(matrixStack, component)
        }.constrain {
            x = 1.pixels()
            y = 1.pixels()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 3.5.pixels()
        }).addChild(hueIndicator)

        hueLine.onMouseClick { event ->
            draggingHue = true
            currentHue = (event.relativeY - 1f) / hueLine.getHeight()
            isChroma = false
            updateHueIndicator(hueIndicator)
        }.onMouseDrag { _, mouseY, _ ->
            if (!draggingHue) return@onMouseDrag
            currentHue = ((mouseY - 1f) / hueLine.getHeight()).coerceIn(0f..1f)
            updateHueIndicator(hueIndicator)
        }.onMouseRelease { draggingHue = false }

        val alphaLine = UIBlock().constrain {
            x = SiblingConstraint(5f)
            width = 14.pixels()
            height = 75.percent()
            color = theme.border.toConstraint()
        }.childOf(this)

        val alphaIndicator = UIText("◄").constrain {
            x = (-4).pixels(alignOpposite = true)
            y = RelativeConstraint(1f - currentAlpha) - 5.pixels()
            color = theme.accent.toConstraint()
        }

        alphaLine.addChild(createCustomRenderer { matrixStack, component ->
            drawAlphaLine(matrixStack, component)
        }.constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        }).addChild(alphaIndicator)

        alphaLine.onMouseClick { event ->
            draggingAlpha = true
            currentAlpha = 1f - ((event.relativeY - 1f) / alphaLine.getHeight())
            updateAlphaIndicator(alphaIndicator)
        }.onMouseDrag { _, mouseY, _ ->
            if (!draggingAlpha) return@onMouseDrag
            currentAlpha = 1f - ((mouseY - 1f) / alphaLine.getHeight()).coerceIn(0f..1f)
            updateAlphaIndicator(alphaIndicator)
        }.onMouseRelease { draggingAlpha = false }

        UIBlock(initialColor).constrain {
            x = SiblingConstraint(-12f, true)
            y = 80.percent() + 4.pixels()
            width = 10.pixels()
            height = 10.pixels()
        }.childOf(this).effect(OutlineEffect(theme.border, 1f)).onMouseClick {
            isChroma = true
            onValueChange(theme.accent)
        }
    }

    private fun createCustomRenderer(renderer: (UMatrixStack, UIComponent) -> Unit) = object : UIComponent() {
        override fun draw(matrixStack: UMatrixStack) {
            super.beforeDraw(matrixStack)
            renderer(matrixStack, this)
            super.draw(matrixStack)
        }
    }

    private fun updateHueIndicator(indicator: UIText) {
        indicator.setY(RelativeConstraint(currentHue.coerceAtMost(0.98f)) - 3.pixels())
        recalculateColor()
    }

    private fun updateAlphaIndicator(indicator: UIText) {
        indicator.setY(RelativeConstraint(1f - currentAlpha.coerceAtMost(0.98f)) - 3.pixels())
        recalculateColor()
    }

    private fun updatePickerIndicator(indicator: UIContainer) {
        indicator.setX((RelativeConstraint(currentSaturation) - 2.5f.pixels()).coerceIn(2.pixels(), 2.pixels(alignOpposite = true)))
        indicator.setY((RelativeConstraint(1f - currentBrightness) - 2.5f.pixels()).coerceIn(2.pixels(), 2.pixels(alignOpposite = true)))
        recalculateColor()
    }

    private fun recalculateColor() {
        val alpha = ((currentAlpha * 255).roundToInt()).coerceIn(0, 255)

        val color = if (isChroma) {
            Color(
                theme.accent.red.coerceIn(0, 255),
                theme.accent.green.coerceIn(0, 255),
                theme.accent.blue.coerceIn(0, 255),
                alpha
            )
        } else {
            val rgb = Color.HSBtoRGB(
                currentHue.coerceIn(0.0f, 1.0f),
                currentSaturation.coerceIn(0.0f, 1.0f),
                currentBrightness.coerceIn(0.0f, 1.0f)
            )
            val c = Color(rgb)
            Color(c.red.coerceIn(0, 255), c.green.coerceIn(0, 255), c.blue.coerceIn(0, 255), alpha)
        }

        currentColor = color
        onValueChange(color)
    }

    fun onValueChange(listener: (Color) -> Unit) {
        onValueChange = listener
    }

    private fun drawColorPicker(matrixStack: UMatrixStack, component: UIComponent) {
        val left = component.getLeft().toDouble()
        val top = component.getTop().toDouble()
        val right = component.getRight().toDouble()
        val bottom = component.getBottom().toDouble()

        val height = bottom - top

        for (x in 0..49) {
            val leftX = left + (right - left) * x / 50f
            val rightX = left + (right - left) * (x + 1) / 50f

            var first = true
            for (y in 0..50) {
                val yPos = top + (y * height / 50.0)
                val color = Color(Color.HSBtoRGB(currentHue, x / 50f, 1 - y / 50f))

                if (!first) {
                    UIBlock.drawBlock(matrixStack, color, leftX, yPos, rightX, yPos + height / 50.0)
                }
                first = false
            }
        }
    }

    private fun drawHueLine(matrixStack: UMatrixStack, component: UIComponent) {
        val left = component.getLeft().toDouble()
        val top = component.getTop().toDouble()
        val right = component.getRight().toDouble()
        val height = component.getHeight().toDouble()

        for (i in 0..50) {
            val yPos = top + (i * height / 50.0)
            val color = Color(Color.HSBtoRGB(i / 50f, 1f, 0.8f))
            UIBlock.drawBlock(matrixStack, color, left, yPos, right, yPos + height / 50.0)
        }
    }

    private fun drawAlphaLine(matrixStack: UMatrixStack, component: UIComponent) {
        val left = component.getLeft().toDouble()
        val top = component.getTop().toDouble()
        val width = component.getWidth().toDouble()
        val height = component.getHeight().toDouble()
        val rectSize = 2.0

        for (y in 0 until (height / rectSize).toInt()) {
            for (x in 0 until (width / rectSize).toInt()) {
                val baseColor = if ((x + y) % 2 == 0) theme.panel else theme.element
                UIBlock.drawBlock(
                    matrixStack,
                    baseColor,
                    left + x * rectSize,
                    top + y * rectSize,
                    left + (x + 1) * rectSize,
                    top + (y + 1) * rectSize
                )
            }
        }

        for (y in 0 until height.toInt()) {
            val alpha = (255 * (1f - y / height)).roundToInt().coerceIn(0, 255)
            UIBlock.drawBlock(
                matrixStack,
                Color(currentColor.red, currentColor.green, currentColor.blue, alpha),
                left,
                top + y,
                left + width,
                top + y + 1
            )
        }
    }
}