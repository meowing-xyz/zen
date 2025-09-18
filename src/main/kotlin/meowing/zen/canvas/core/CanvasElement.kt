package meowing.zen.canvas.core

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.ChatUtils

enum class Size {
    Auto,
    ParentPerc,
    ScreenPerc,
    Pixels
}

enum class Pos {
    ParentPercent,
    ScreenPercent,
    ParentPixels,
    ScreenPixels,
    ParentCenter,
    ScreenCenter
}

abstract class CanvasElement<T : CanvasElement<T>>(
    var widthType: Size = Size.Pixels,
    var heightType: Size = Size.Pixels
) {
    protected val children: MutableList<CanvasElement<*>> = mutableListOf()

    var xPositionConstraint = Pos.ParentPixels
    var yPositionConstraint = Pos.ParentPixels
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f
    var widthPercent: Float = 100f
    var heightPercent: Float = 100f
    var absoluteX: Float = 0f
    var absoluteY: Float = 0f
    var visible: Boolean = true

    var isHovered: Boolean = false
    var isPressed: Boolean = false
    var isFocused: Boolean = false

    val screenWidth: Int get() = mc.displayWidth
    val screenHeight: Int get() = mc.displayHeight

    var parent: CanvasElement<*>? = null

    var onMouseEnter: ((Float, Float) -> Unit)? = null
    var onMouseExit: ((Float, Float) -> Unit)? = null
    var onMouseMove: ((Float, Float) -> Unit)? = null
    var onMouseScroll: ((Float, Float, Double, Double) -> Boolean)? = null
    var onMouseClick: ((Float, Float, Int) -> Boolean)? = null
    var onMouseRelease: ((Float, Float, Int) -> Boolean)? = null
    var onKeyPress: ((Int, Int, Int) -> Boolean)? = null
    var onKeyRelease: ((Int, Int, Int) -> Boolean)? = null
    var onCharType: ((Char) -> Boolean)? = null

    init {
        if (parent == null) {
            EventDispatcher.registerRoot(this)
        }
    }

    fun destroy() {
        if (parent == null) {
            EventDispatcher.unregisterRoot(this)
        }
        children.forEach { it.destroy() }
        children.clear()
    }

    fun findFirstVisibleParent(): CanvasElement<*>? {
        var current = parent
        while (current != null) {
            if (current.visible) return current
            current = current.parent
        }
        return null
    }

    open fun updateWidth() {
        width = when (widthType) {
            Size.Auto -> getAutoWidth()
            Size.ParentPerc -> findFirstVisibleParent()?.width?.times(widthPercent / 100f) ?: width
            Size.Pixels -> width
            Size.ScreenPerc -> screenWidth * (widthPercent / 100f)
        }
    }

    open fun updateHeight() {
        height = when (heightType) {
            Size.Auto -> getAutoHeight()
            Size.ParentPerc -> findFirstVisibleParent()?.height?.times(heightPercent / 100f) ?: height
            Size.Pixels -> height
            Size.ScreenPerc -> screenHeight * (heightPercent / 100f)
        }
    }

    protected open fun getAutoWidth(): Float =
        children.filter { it.visible }.maxOfOrNull { it.x + it.width }?.coerceAtLeast(0f) ?: 0f

    protected open fun getAutoHeight(): Float =
        children.filter { it.visible }.maxOfOrNull { it.y + it.height }?.coerceAtLeast(0f) ?: 0f

    fun updateX() {
        val visibleParent = findFirstVisibleParent()
        x = when (xPositionConstraint) {
            Pos.ParentPercent -> visibleParent?.let { it.x + (it.width * (x / 100f)) } ?: x
            Pos.ScreenPercent -> screenWidth * (x / 100f)
            Pos.ParentPixels -> x
            Pos.ScreenPixels -> x
            Pos.ParentCenter -> visibleParent?.let { it.x + (it.width / 2f) - (width / 2f) + x } ?: x
            Pos.ScreenCenter -> (screenWidth / 2f) - (width / 2f)
        }
    }

    fun updateY() {
        val visibleParent = findFirstVisibleParent()
        y = when (yPositionConstraint) {
            Pos.ParentPercent -> visibleParent?.let { it.y + (it.height * (y / 100f)) } ?: y
            Pos.ScreenPercent -> screenHeight * (y / 100f)
            Pos.ParentPixels -> y
            Pos.ScreenPixels -> y
            Pos.ParentCenter -> visibleParent?.let { it.y + (it.height / 2f) - (height / 2f) + y } ?: y
            Pos.ScreenCenter -> (screenHeight / 2f) - (height / 2f)
        }
    }

    fun isPointInside(mouseX: Float, mouseY: Float): Boolean =
        mouseX >= absoluteX && mouseX <= absoluteX + width && mouseY >= absoluteY && mouseY <= absoluteY + height

    open fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        if (!visible) return false

        val wasHovered = isHovered
        isHovered = isPointInside(mouseX, mouseY)
        ChatUtils.addMessage("$absoluteX, $absoluteY, $mouseX, $mouseY, $isHovered")

        when {
            isHovered && !wasHovered -> onMouseEnter?.invoke(mouseX, mouseY)
            !isHovered && wasHovered -> onMouseExit?.invoke(mouseX, mouseY)
        }

        if (isHovered) onMouseMove?.invoke(mouseX, mouseY)

        return children.reversed().any { it.handleMouseMove(mouseX, mouseY) } || isHovered
    }

    open fun handleMouseClick(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any { it.handleMouseClick(mouseX, mouseY, button) }
        ChatUtils.addMessage("$absoluteX, $absoluteY, $mouseX, $mouseY, $isHovered")

        return when {
            childHandled -> true
            isPointInside(mouseX, mouseY) -> {
                isPressed = true
                focus()
                onMouseClick?.invoke(mouseX, mouseY, button) ?: true
            }
            else -> false
        }
    }

    open fun handleMouseRelease(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        val wasPressed = isPressed
        isPressed = false

        val childHandled = children.reversed().any { it.handleMouseRelease(mouseX, mouseY, button) }
        return childHandled || (wasPressed && isPointInside(mouseX, mouseY) && (onMouseRelease?.invoke(mouseX, mouseY, button) ?: true))
    }

    open fun handleMouseScroll(mouseX: Float, mouseY: Float, horizontal: Double, vertical: Double): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any { it.handleMouseScroll(mouseX, mouseY, horizontal, vertical) }
        return childHandled || (isPointInside(mouseX, mouseY) && (onMouseScroll?.invoke(mouseX, mouseY, horizontal, vertical) ?: false))
    }

    open fun handleKeyPress(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!visible || !isFocused) return false
        return onKeyPress?.invoke(keyCode, scanCode, modifiers) ?: false || children.reversed().any { it.handleKeyPress(keyCode, scanCode, modifiers) }
    }

    open fun handleKeyRelease(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!visible || !isFocused) return false
        return onKeyRelease?.invoke(keyCode, scanCode, modifiers) ?: false || children.reversed().any { it.handleKeyRelease(keyCode, scanCode, modifiers) }
    }

    open fun handleCharType(char: Char): Boolean {
        if (!visible || !isFocused) return false
        return onCharType?.invoke(char) ?: false || children.reversed().any { it.handleCharType(char) }
    }

    fun focus() {
        parent?.children?.forEach { it.isFocused = false }
        isFocused = true
    }

    fun unfocus() {
        isFocused = false
    }

    open fun render(mouseX: Float, mouseY: Float) {
        if (!visible) return

        updateHeight()
        updateWidth()
        updateX()
        updateY()

        val visibleParent = findFirstVisibleParent()
        absoluteX = (visibleParent?.absoluteX ?: 0f) + x
        absoluteY = (visibleParent?.absoluteY ?: 0f) + y

        onRender(mouseX, mouseY)
        renderChildren(mouseX, mouseY)
    }

    protected open fun renderChildren(mouseX: Float, mouseY: Float) {
        children.forEach { it.render(mouseX, mouseY) }
    }

    protected abstract fun onRender(mouseX: Float, mouseY: Float)

    fun childOf(parent: CanvasElement<*>): T = apply { parent.addChild(this) } as T

    fun addChild(child: CanvasElement<*>): T = apply {
        if (child.parent == null) {
            EventDispatcher.unregisterRoot(child)
        }
        child.parent = this
        children.add(child)
    } as T

    fun setSizing(widthType: Size, heightType: Size): T = apply {
        this.widthType = widthType
        this.heightType = heightType
    } as T

    fun setSizing(width: Float, widthType: Size, height: Float, heightType: Size): T = apply {
        this.widthType = widthType
        this.heightType = heightType
        if (widthType == Size.Pixels) this.width = width else this.widthPercent = width
        if (heightType == Size.Pixels) this.height = height else this.heightPercent = height
    } as T

    fun setPositioning(xConstraint: Pos, yConstraint: Pos): T = apply {
        this.xPositionConstraint = xConstraint
        this.yPositionConstraint = yConstraint
    } as T

    fun setPositioning(x: Float, xConstraint: Pos, y: Float, yConstraint: Pos): T = apply {
        this.x = x
        this.y = y
        this.xPositionConstraint = xConstraint
        this.yPositionConstraint = yConstraint
    } as T

    fun onHover(onEnter: (Float, Float) -> Unit, onExit: (Float, Float) -> Unit = { _, _ -> }): T = apply {
        this.onMouseEnter = onEnter
        this.onMouseExit = onExit
    } as T

    fun onClick(callback: (Float, Float, Int) -> Boolean): T = apply {
        this.onMouseClick = callback
    } as T

    fun onRelease(callback: (Float, Float, Int) -> Boolean): T = apply {
        this.onMouseRelease = callback
    } as T

    fun onKey(callback: (Int, Int, Int) -> Boolean): T = apply {
        this.onKeyPress = callback
    } as T

    fun onChar(callback: (Char) -> Boolean): T = apply {
        this.onCharType = callback
    } as T

    fun onScroll(callback: (Float, Float, Double, Double) -> Boolean): T = apply {
        this.onMouseScroll = callback
    } as T

    fun show(): T = apply { visible = true } as T
    fun hide(): T = apply { visible = false } as T

    val hovered: Boolean get() = isHovered
    val pressed: Boolean get() = isPressed
    val focused: Boolean get() = isFocused
}