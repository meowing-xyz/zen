package meowing.zen.utils.gui.components

import meowing.zen.utils.gui.Size
import meowing.zen.utils.gui.NanoGuiElement
import meowing.zen.utils.gui.Pos
import meowing.zen.utils.rendering.NVGRenderer
import meowing.zen.utils.rendering.Font

class NanoText(
    private var text: String,
    var textColor: Int = 0xFFFFFFFF.toInt(),
    var fontSize: Float = 12f,
    var shadowEnabled: Boolean = false,
    var font: Font = NVGRenderer.defaultFont
) : NanoGuiElement<NanoText>() {

    init {
        this.widthType = Size.Pixels
        this.heightType = Size.Pixels
        this.xPositionConstraint = Pos.ParentPixels
        this.yPositionConstraint = Pos.ParentPixels
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (text.isEmpty()) return

        if (shadowEnabled) {
            NVGRenderer.textShadow(text, absoluteX, absoluteY, fontSize, textColor, font)
        } else {
            NVGRenderer.text(text, absoluteX, absoluteY, fontSize, textColor, font)
        }
    }

    fun setText(newText: String) {
        text = newText
        updateTextWidth()
    }

    fun updateTextWidth() {
        width = NVGRenderer.textWidth(text, fontSize, font)
        height = fontSize
    }
}