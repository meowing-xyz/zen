package meowing.zen.canvas.core.components

import meowing.zen.canvas.core.CanvasElement
import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.utils.rendering.NVGRenderer
import meowing.zen.utils.rendering.Font

class Text(
    var text: String = "",
    var textColor: Int = 0xFFFFFFFF.toInt(),
    var fontSize: Float = 12f,
    var shadowEnabled: Boolean = false,
    var font: Font = NVGRenderer.defaultFont
) : CanvasElement<Text>() {

    init {
        setSizing(Size.Auto, Size.Auto)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        ignoreMouseEvents()
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (text.isEmpty()) return

        if (shadowEnabled) {
            NVGRenderer.textShadow(text, x, y, fontSize, textColor, font)
        } else {
            NVGRenderer.text(text, x, y, fontSize, textColor, font)
        }
        // Draw border for debugging
//        NVGRenderer.hollowRect(x, y, width, height, 1f, 0xFFFF00FF.toInt(), 0f)
    }

    override fun getAutoWidth(): Float = NVGRenderer.textWidth(text, fontSize, font)

    override fun getAutoHeight(): Float = fontSize

    fun text(newText: String): Text = apply {
        text = newText
    }

    fun color(color: Int): Text = apply {
        textColor = color
    }

    fun fontSize(size: Float): Text = apply {
        fontSize = size
    }

    fun font(newFont: Font): Text = apply {
        font = newFont
    }

    fun shadow(enabled: Boolean = true): Text = apply {
        shadowEnabled = enabled
    }
}