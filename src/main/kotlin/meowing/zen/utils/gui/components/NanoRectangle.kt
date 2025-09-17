package meowing.zen.utils.gui.components

import meowing.zen.utils.gui.Size
import meowing.zen.utils.gui.NanoGuiElement
import meowing.zen.utils.rendering.NVGRenderer

class NanoRectangle(
    var backgroundColor: Int = 0x80000000.toInt(),
    var borderColor: Int = 0xFFFFFFFF.toInt(),
    var borderRadius: Float = 0f,
    var borderThickness: Float = 0f,
    var padding: FloatArray = floatArrayOf(0f, 0f, 0f, 0f), // top, right, bottom, left
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto,
) : NanoGuiElement<NanoRectangle>(widthType, heightType) {

    override fun onRender(mouseX: Float, mouseY: Float) {
        // Render background
        if (backgroundColor != 0) {
            NVGRenderer.rect(absoluteX, absoluteY, width, height, backgroundColor, borderRadius)
        }

        // Render border
        if (borderThickness > 0f) {
            NVGRenderer.hollowRect(absoluteX, absoluteY, width, height, borderThickness, borderColor, borderRadius)
        }
    }

    override fun updateWidth() {
        width = when (widthType) {
            Size.Auto -> {
                val contentWidth = children.maxOfOrNull { it.x + it.width } ?: 0f
                contentWidth + padding[3] + padding[1] // left + right
            }
            Size.ParentPerc -> parent?.let { it.width * (width / 100f) } ?: width
            Size.Pixels -> width
            Size.ScreenPerc -> {
                screenWidth * (widthPercent / 100f)
            }
        }
    }

    override fun updateHeight() {
        height = when (heightType) {
            Size.Auto -> {
                val contentHeight = children.maxOfOrNull { it.y + it.height } ?: 0f
                contentHeight + padding[0] + padding[2] // top + bottom
            }
            Size.ParentPerc -> parent?.let { it.height * (height / 100f) } ?: height
            Size.Pixels -> height
            Size.ScreenPerc -> screenHeight * (heightPercent / 100f)
        }
    }

    fun setPadding(top: Float=0f, right: Float=0f, bottom: Float=0f, left: Float=0f): NanoRectangle {
        padding[0] = top
        padding[1] = right
        padding[2] = bottom
        padding[3] = left
        return this
    }

    override fun render(mouseX: Float, mouseY: Float) {
        // Update self
        updateWidth()
        updateHeight()
        updateX()
        updateY()

        // Update absolute positions
        absoluteX = (parent?.absoluteX ?: 0f) + x
        absoluteY = (parent?.absoluteY ?: 0f) + y

        // Render self
        onRender(mouseX, mouseY)

        // Render children with padding applied
        children.forEach { child ->
            val oldX = child.x
            val oldY = child.y

            // Offset child by left/top padding
            child.x += padding[3] // left
            child.y += padding[0] // top

            child.render(mouseX, mouseY)

            // Restore child position
            child.x = oldX
            child.y = oldY
        }
    }
}