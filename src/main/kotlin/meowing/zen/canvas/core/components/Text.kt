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
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (text.isEmpty()) return

        if (shadowEnabled) {
            NVGRenderer.textShadow(text, absoluteX, absoluteY, fontSize, textColor, font)
        } else {
            NVGRenderer.text(text, absoluteX, absoluteY, fontSize, textColor, font)
        }
    }

    override fun getAutoWidth(): Float = NVGRenderer.textWidth(text, fontSize, font)

    override fun getAutoHeight(): Float = fontSize

    fun text(newText: String): Text = apply {
        text = newText
    }

    fun color(color: Int): Text = apply {
        textColor = color
    }

    fun size(size: Float): Text = apply {
        fontSize = size
    }

    fun font(newFont: Font): Text = apply {
        font = newFont
    }

    fun shadow(enabled: Boolean = true): Text = apply {
        shadowEnabled = enabled
    }
}