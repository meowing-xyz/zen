package meowing.zen.utils.gui.components

import meowing.zen.utils.gui.SizeConstraint
import meowing.zen.utils.gui.NanoGuiElement
import meowing.zen.utils.gui.PositionConstraint
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
        this.widthType = SizeConstraint.Pixels
        this.heightType = SizeConstraint.Pixels
        this.xPositionConstraint = PositionConstraint.ParentPixels
        this.yPositionConstraint = PositionConstraint.ParentPixels
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