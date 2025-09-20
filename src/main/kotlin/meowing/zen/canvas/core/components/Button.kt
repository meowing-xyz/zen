package meowing.zen.canvas.core.components

import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.utils.rendering.NVGRenderer
import meowing.zen.utils.rendering.Font

class Button(
    var text: String = "",
    var textColor: Int = 0xFFFFFFFF.toInt(),
    var fontSize: Float = 12f,
    var font: Font = NVGRenderer.defaultFont,
    var hoverTextColor: Int? = null,
    var pressedTextColor: Int? = null,
    var shadowEnabled: Boolean = false,
    backgroundColor: Int = 0x80404040.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 1f,
    padding: FloatArray = floatArrayOf(8f, 16f, 8f, 16f),
    hoverColor: Int? = 0x80505050.toInt(),
    pressedColor: Int? = 0x80303030.toInt(),
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto
) : Rectangle(backgroundColor, borderColor, borderRadius, borderThickness, padding, hoverColor, pressedColor, widthType, heightType) {

    init {
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        super.onRender(mouseX, mouseY)

        if (text.isNotEmpty()) {
            val currentTextColor = when {
                pressed && pressedTextColor != null -> pressedTextColor!!
                hovered && hoverTextColor != null -> hoverTextColor!!
                else -> textColor
            }

            val textWidth = NVGRenderer.textWidth(text, fontSize, font)
            val textX = absoluteX + (width - textWidth) / 2f
            val textY = absoluteY + (height - fontSize) / 2f

            if (shadowEnabled) {
                NVGRenderer.textShadow(text, textX, textY, fontSize, currentTextColor, font)
            } else {
                NVGRenderer.text(text, textX, textY, fontSize, currentTextColor, font)
            }
        }
    }

    override fun getAutoWidth(): Float {
        val textWidth = if (text.isNotEmpty()) NVGRenderer.textWidth(text, fontSize, font) else 0f
        val contentWidth = maxOf(textWidth, children.maxOfOrNull { it.x + it.width } ?: 0f)

        return contentWidth + padding[1] + padding[3]
    }

    override fun getAutoHeight(): Float {
        val textHeight = if (text.isNotEmpty()) fontSize else 0f
        val contentHeight = maxOf(textHeight, children.maxOfOrNull { it.y + it.height } ?: 0f)

        return contentHeight + padding[0] + padding[2]
    }

    fun text(text: String): Button = apply {
        this.text = text
    }

    fun textColor(color: Int): Button = apply {
        this.textColor = color
    }

    fun fontSize(size: Float): Button = apply {
        this.fontSize = size
    }

    fun font(font: Font): Button = apply {
        this.font = font
    }

    fun hoverColors(bg: Int? = null, text: Int? = null): Button = apply {
        this.hoverColor = bg
        this.hoverTextColor = text
    }

    fun pressedColors(bg: Int? = null, text: Int? = null): Button = apply {
        this.pressedColor = bg
        this.pressedTextColor = text
    }

    fun shadow(enabled: Boolean = true): Button = apply {
        this.shadowEnabled = enabled
    }

    override fun padding(top: Float, right: Float, bottom: Float, left: Float): Button = apply {
        super.padding(top, right, bottom, left)
    }

    override fun padding(all: Float): Button = apply {
        super.padding(all)
    }

    override fun backgroundColor(color: Int): Button = apply {
        super.backgroundColor(color)
    }

    override fun borderColor(color: Int): Button = apply {
        super.borderColor(color)
    }

    override fun borderRadius(radius: Float): Button = apply {
        super.borderRadius(radius)
    }

    override fun borderThickness(thickness: Float): Button = apply {
        super.borderThickness(thickness)
    }

    override fun hoverColor(color: Int): Button = apply {
        super.hoverColor(color)
    }

    override fun pressedColor(color: Int): Button = apply {
        super.pressedColor(color)
    }

    override fun width(newWidth: Float): Button = apply {
        super.width(newWidth)
    }

    override fun height(newHeight: Float): Button = apply {
        super.height(newHeight)
    }
}