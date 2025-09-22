package meowing.zen.canvas.core.components

import meowing.zen.canvas.core.CanvasElement
import meowing.zen.canvas.core.Pos
import meowing.zen.canvas.core.Size
import meowing.zen.utils.rendering.NVGRenderer
import java.awt.Color
import java.util.UUID

class SvgImage(
    var svgPath: String = "",
    var startingWidth: Float = 80f,
    var startingHeight: Float = 80f,
    var color: Color = Color.WHITE
) : CanvasElement<SvgImage>() {
    var imageId = "${UUID.randomUUID()}"
    var image = NVGRenderer.createImage(svgPath, startingWidth.toInt(), startingHeight.toInt(), color, imageId)

    init {
        width = startingWidth
        height = startingHeight
        setSizing(Size.Auto, Size.Auto)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        ignoreMouseEvents()
        setSizing(startingWidth, Size.Pixels, startingHeight, Size.Pixels)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (svgPath.isEmpty()) return

        startingWidth = width
        startingHeight = height

        NVGRenderer.svg(imageId, x, y, startingWidth, startingHeight, color.alpha / 255f) // Draw the SVG with a red tint
    }

    fun setSvgColor(newColor: Color) {
        if (color != newColor) {
            color = newColor
            reloadImage()
        }
    }

    private fun reloadImage() {
        image = NVGRenderer.createImage(svgPath, startingWidth.toInt(), startingHeight.toInt(), color, imageId)
    }
}