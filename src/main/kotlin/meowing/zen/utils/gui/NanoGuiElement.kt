package meowing.zen.utils.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution

enum class SizeConstraint {
    AutoContent,
    ParentPercent,
    ScreenPercent,
    Pixels
}

enum class PositionConstraint {
    ParentPercent,
    ScreenPercent,
    ParentPixels,
    ScreenPixels,
    ParentCenter,
    ScreenCenter
}

abstract class NanoGuiElement<T : NanoGuiElement<T>>(
    var widthType: SizeConstraint = SizeConstraint.Pixels,
    var heightType: SizeConstraint = SizeConstraint.Pixels
) {
    // Position and dimensions
    var xPositionConstraint = PositionConstraint.ParentPixels
    var yPositionConstraint = PositionConstraint.ParentPixels
    var x: Float = 0f
    var y: Float = 0f

    var width: Float = 0f
    var height: Float = 0f

    var widthPercent: Float = 100f
    var heightPercent: Float = 100f
    var absoluteX: Float = 0f  // Absolute position on screen
    var absoluteY: Float = 0f  // Absolute position on screen

    val scaledResolution: ScaledResolution
        get() = ScaledResolution(Minecraft.getMinecraft())
    val screenWidth: Int
        get() = scaledResolution.scaledWidth * 2
    val screenHeight: Int
        get() = scaledResolution.scaledHeight * 2

    // Parent-child relationships
    var parent: NanoGuiElement<*>? = null
    protected val children: MutableList<NanoGuiElement<*>> = mutableListOf()

    open fun updateWidth() {
        width = when (widthType) {
            SizeConstraint.AutoContent -> children.maxOfOrNull { it.x + it.width }?.coerceAtLeast(0f) ?: 0f
            SizeConstraint.ParentPercent -> parent?.let { it.width * (width / 100f) } ?: width
            SizeConstraint.Pixels -> width
            SizeConstraint.ScreenPercent -> screenWidth * (widthPercent / 100f)
        }
    }

    open fun updateHeight() {
        height = when (heightType) {
            SizeConstraint.AutoContent -> children.maxOfOrNull { it.y + it.height }?.coerceAtLeast(0f) ?: 0f
            SizeConstraint.ParentPercent -> parent?.let { it.height * (height / 100f) } ?: height
            SizeConstraint.Pixels -> height
            SizeConstraint.ScreenPercent -> screenHeight * (heightPercent / 100f)
        }
    }

    /** Updates X position based on constraint */
    fun updateX() {
        x = when (xPositionConstraint) {
            PositionConstraint.ParentPercent -> parent?.let { it.x + (it.width * (x / 100f)) } ?: x
            PositionConstraint.ScreenPercent -> screenWidth * (x / 100f)
            PositionConstraint.ParentPixels -> x
            PositionConstraint.ScreenPixels -> x
            PositionConstraint.ParentCenter -> parent?.let { it.x + (it.width / 2f) - (width / 2f) + x } ?: x
            PositionConstraint.ScreenCenter -> (screenWidth / 2f) - (width / 2f)
        }
    }

    /** Updates Y position based on constraint */
    fun updateY() {
        y = when (yPositionConstraint) {
            PositionConstraint.ParentPercent -> parent?.let { it.y + (it.height * (y / 100f)) } ?: y
            PositionConstraint.ScreenPercent -> screenHeight * (y / 100f)
            PositionConstraint.ParentPixels -> y
            PositionConstraint.ScreenPixels -> y
            PositionConstraint.ParentCenter -> parent?.let { it.y + (it.height / 2f) - (height / 2f) + y } ?: y
            PositionConstraint.ScreenCenter -> (screenHeight / 2f) - (height / 2f)
        }
    }

    /** Main render method */
    open fun render(mouseX: Float, mouseY: Float) {
        updateHeight()
        updateWidth()
        updateX()
        updateY()

        // Update absolute positions
        absoluteX = (parent?.absoluteX ?: 0f) + x
        absoluteY = (parent?.absoluteY ?: 0f) + y

        onRender(mouseX, mouseY) // No need to pass absolute positions

        // Render children
        children.forEach { child ->
            child.render(mouseX, mouseY)
        }
    }

    /** Override this method to implement custom rendering */
    protected abstract fun onRender(mouseX: Float, mouseY: Float)

    /** Set parent-child relationship */
    fun childOf(parent: NanoGuiElement<*>): T {
        parent.addChild(this)
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    fun addChild(child: NanoGuiElement<*>): T {
        child.parent = this
        children.add(child)
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    /** Set sizing type */
    fun setSizing(widthType: SizeConstraint, heightType: SizeConstraint): T {
        this.widthType = widthType
        this.heightType = heightType
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    fun setSizing(
        width: Float, widthType: SizeConstraint,
        height: Float, heightType: SizeConstraint
    ): T {
        this.widthType = widthType
        this.heightType = heightType
        if (widthType == SizeConstraint.Pixels) this.width = width else this.widthPercent = width
        if (heightType == SizeConstraint.Pixels) this.height = height else this.heightPercent = height
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    /** Set position constraints */
    fun setPositioning(
        xPositionConstraint: PositionConstraint,
        yPositionConstraint: PositionConstraint): T {
        this.xPositionConstraint = xPositionConstraint
        this.yPositionConstraint = yPositionConstraint
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    /** Set position constraints */
    fun setPositioning(
        x: Float, xPositionConstraint: PositionConstraint,
        y: Float, yPositionConstraint: PositionConstraint): T {
        this.xPositionConstraint = xPositionConstraint
        this.yPositionConstraint = yPositionConstraint
        this.x = x
        this.y = y
        @Suppress("UNCHECKED_CAST")
        return this as T
    }
}