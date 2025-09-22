package meowing.zen.canvas.core

import meowing.zen.Zen.Companion.mc
import meowing.zen.canvas.core.animations.EasingType
import meowing.zen.canvas.core.animations.fadeIn
import meowing.zen.canvas.core.animations.fadeOut
import meowing.zen.canvas.core.components.Rectangle
import meowing.zen.canvas.core.components.Tooltip

enum class Size {
    Auto,
    ParentPerc,
    Pixels
}

enum class Pos {
    ParentPercent,
    ScreenPercent,
    ParentPixels,
    ScreenPixels,
    ParentCenter,
    ScreenCenter,
    AfterSibling,
    MatchSibling
}

abstract class CanvasElement<T : CanvasElement<T>>(
    var widthType: Size = Size.Pixels,
    var heightType: Size = Size.Pixels
) {
    val children: MutableList<CanvasElement<*>> = mutableListOf()
    val topChildren: MutableList<CanvasElement<*>> = mutableListOf()

    var xPositionConstraint = Pos.ParentPixels
    var yPositionConstraint = Pos.ParentPixels
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f
    var widthPercent: Float = 100f
    var heightPercent: Float = 100f
    var visible: Boolean = true

    var xConstraint: Float = 0f
    var yConstraint: Float = 0f

    var isHovered: Boolean = false
    var isPressed: Boolean = false
    var isFocused: Boolean = false
    var isFloating: Boolean = false
    var ignoreFocus: Boolean = false
    var renderOnTop: Boolean = false
    var requiresFocus: Boolean = false

    val screenWidth: Int get() = mc.displayWidth
    val screenHeight: Int get() = mc.displayHeight

    var parent: CanvasElement<*>? = null
    var tooltipElement: Tooltip? = null

    val mouseEnterListeners = mutableListOf<(Float, Float) -> Unit>()
    val mouseExitListeners = mutableListOf<(Float, Float) -> Unit>()
    val mouseMoveListeners = mutableListOf<(Float, Float) -> Unit>()
    val mouseScrollListeners = mutableListOf<(Float, Float, Double, Double) -> Boolean>()
    val mouseClickListeners = mutableListOf<(Float, Float, Int) -> Boolean>()
    val mouseReleaseListeners = mutableListOf<(Float, Float, Int) -> Boolean>()
    val charTypeListeners = mutableListOf<(Int, Int, Char) -> Boolean>()

    var onValueChange: ((Any) -> Unit)? = null

    init {
        if (parent == null) EventDispatcher.registerRoot(this)
    }

    open fun destroy() {
        EventDispatcher.unregisterRoot(this)
        children.forEach { it.destroy() }
        topChildren.forEach { it.destroy() }
        children.clear()
        topChildren.clear()
        mouseEnterListeners.clear()
        mouseExitListeners.clear()
        mouseMoveListeners.clear()
        mouseScrollListeners.clear()
        mouseClickListeners.clear()
        mouseReleaseListeners.clear()
        charTypeListeners.clear()
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
            Size.ParentPerc -> {
                if (parent == null) {
                    screenWidth * (widthPercent / 100f)
                } else {
                    val thisparent = findFirstVisibleParent()!!
                    var modifiedWidth = thisparent.width.times(widthPercent / 100f)

                    if (thisparent is Rectangle) modifiedWidth -= (thisparent.padding[1] + thisparent.padding[3])
                    modifiedWidth
                }
            }
            Size.Pixels -> width
        }
    }

    open fun updateHeight() {
        height = when (heightType) {
            Size.Auto -> getAutoHeight()
            Size.ParentPerc -> {
                if (parent == null) {
                    screenHeight * (heightPercent / 100f)
                } else {
                    val thisparent = findFirstVisibleParent()!!
                    var modifiedHeight = thisparent.height.times(heightPercent / 100f)

                    if (thisparent is Rectangle) modifiedHeight -= (thisparent.padding[0] + thisparent.padding[2])
                    modifiedHeight
                }
            }
            Size.Pixels -> height
        }
    }

    protected open fun getAutoWidth(): Float =
        children.filter { it.visible && !it.isFloating }.maxOfOrNull { it.x + it.width }?.coerceAtLeast(0f) ?: 0f

    protected open fun getAutoHeight(): Float =
        children.filter { it.visible && !it.isFloating }.maxOfOrNull { it.y + it.height }?.coerceAtLeast(0f) ?: 0f

    fun updateX() {
        val visibleParent = findFirstVisibleParent()

        x = when (xPositionConstraint) {
            Pos.ParentPercent -> if (visibleParent != null) visibleParent.x + (visibleParent.width * (xConstraint / 100f)) else xConstraint
            Pos.ScreenPercent -> screenWidth * (xConstraint / 100f)
            Pos.ParentPixels -> if (visibleParent != null) visibleParent.x + xConstraint else xConstraint
            Pos.ScreenPixels -> xConstraint
            Pos.ParentCenter -> {
                if (visibleParent != null) {
                    visibleParent.x + (visibleParent.width - width) / 2f
                } else xConstraint
            }
            Pos.ScreenCenter -> (screenWidth / 2f) - (width / 2f) + xConstraint
            Pos.AfterSibling -> {
                val index = parent?.children?.indexOf(this) ?: -1
                if (index > 0) {
                    val prev = parent!!.children[index - 1]
                    prev.x + prev.width + xConstraint
                } else xConstraint
            }
            Pos.MatchSibling -> {
                val index = parent?.children?.indexOf(this) ?: -1
                if (index > 0) {
                    val prev = parent!!.children[index - 1]
                    prev.x
                } else xConstraint
            }
        }
    }

    fun updateY() {
        val visibleParent = findFirstVisibleParent()

        y = when (yPositionConstraint) {
            Pos.ParentPercent -> if (visibleParent != null) visibleParent.y + (visibleParent.height * (yConstraint / 100f)) else yConstraint
            Pos.ScreenPercent -> screenHeight * (yConstraint / 100f)
            Pos.ParentPixels -> if (visibleParent != null) visibleParent.y + yConstraint else yConstraint
            Pos.ScreenPixels -> yConstraint
            Pos.ParentCenter -> {
                if (visibleParent != null) {
                    visibleParent.y + visibleParent.height / 2f - height / 2f
                } else yConstraint
            }
            Pos.ScreenCenter -> (screenHeight / 2f) - (height / 2f) + yConstraint
            Pos.AfterSibling -> {
                val index = parent?.children?.indexOf(this) ?: -1
                if (index > 0) {
                    val prev = parent!!.children[index - 1]
                    (prev.y + prev.height + yConstraint)
                } else yConstraint
            }
            Pos.MatchSibling -> {
                val index = parent?.children?.indexOf(this) ?: -1
                if (index > 0) {
                    val prev = parent!!.children[index - 1]
                    if (prev is Rectangle) {
                        prev.y + yConstraint - (prev.padding[0] + prev.padding[2])
                    } else prev.y + yConstraint
                } else yConstraint
            }
        }
    }

    fun isPointInside(mouseX: Float, mouseY: Float): Boolean = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height

    open fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        if (!visible) return false

        val wasHovered = isHovered
        isHovered = isPointInside(mouseX, mouseY)

        when {
            isHovered && !wasHovered -> {
                mouseEnterListeners.forEach { it(mouseX, mouseY) }
                tooltipElement?.let {
                    it.fadeIn(200, EasingType.EASE_OUT)
                    it.innerText.fadeIn(200, EasingType.EASE_OUT)
                }
            }
            !isHovered && wasHovered -> {
                mouseExitListeners.forEach { it(mouseX, mouseY) }
                tooltipElement?.let {
                    it.fadeOut(200, EasingType.EASE_OUT)
                    it.innerText.fadeOut(200, EasingType.EASE_OUT)
                }
            }
        }

        if (isHovered) mouseMoveListeners.forEach { it(mouseX, mouseY) }

        return children.reversed().any { it.handleMouseMove(mouseX, mouseY) } || isHovered
    }

    open fun handleMouseClick(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any { it.handleMouseClick(mouseX, mouseY, button) }

        return when {
            childHandled -> true
            isPointInside(mouseX, mouseY) -> {
                isPressed = true
                focus()
                mouseClickListeners.any { it(mouseX, mouseY, button) } || mouseClickListeners.isEmpty()
            }
            else -> {
                if (requiresFocus && isFocused) unfocus()
                false
            }
        }
    }

    open fun handleMouseRelease(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        val wasPressed = isPressed
        isPressed = false

        val childHandled = children.reversed().any { it.handleMouseRelease(mouseX, mouseY, button) }
        return childHandled || (wasPressed && isPointInside(mouseX, mouseY) && (mouseReleaseListeners.any { it(mouseX, mouseY, button) } || mouseReleaseListeners.isEmpty()))
    }

    open fun handleMouseScroll(mouseX: Float, mouseY: Float, horizontal: Double, vertical: Double): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any { it.handleMouseScroll(mouseX, mouseY, horizontal, vertical) }
        return childHandled || (isPointInside(mouseX, mouseY) && mouseScrollListeners.any { it(mouseX, mouseY, horizontal, vertical) })
    }

    open fun handleCharType(keyCode: Int, scanCode: Int, charTyped: Char): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any { it.handleCharType(keyCode, scanCode, charTyped) }
        val selfHandled = if (isFocused || ignoreFocus) charTypeListeners.any { it(keyCode, scanCode, charTyped) } else false

        return childHandled || selfHandled
    }

    fun focus() {
        getRootElement().unfocusAll()
        isFocused = true
    }

    fun unfocus() {
        isFocused = false
    }

    private fun unfocusAll() {
        if (isFocused) unfocus()
        children.forEach { it.unfocusAll() }
        topChildren.forEach { it.unfocusAll() }
    }

    fun getRootElement(): CanvasElement<*> {
        var current: CanvasElement<*> = this

        while (current.parent != null) {
            current = current.parent!!
        }

        return current
    }

    open fun render(mouseX: Float, mouseY: Float) {
        if (!visible) return

        updateHeight()
        updateWidth()
        updateX()
        updateY()

        onRender(mouseX, mouseY)
        renderChildren(mouseX, mouseY)
        renderTopChildren(mouseX, mouseY)
    }

    protected open fun renderChildren(mouseX: Float, mouseY: Float) {
        children.filter { !it.renderOnTop }.forEach { it.render(mouseX, mouseY) }
    }

    protected open fun renderTopChildren(mouseX: Float, mouseY: Float) {
        children.filter { it.renderOnTop }.forEach { it.render(mouseX, mouseY) }
        topChildren.forEach { it.render(mouseX, mouseY) }
    }

    protected abstract fun onRender(mouseX: Float, mouseY: Float)

    fun childOf(parent: CanvasElement<*>): T = apply { parent.addChild(this) } as T

    @Suppress("UNCHECKED_CAST")
    fun addChild(child: CanvasElement<*>): T = apply {
        if (child.parent == null) {
            EventDispatcher.unregisterRoot(child)
        }
        child.parent = this
        if (child.renderOnTop) topChildren.add(child) else children.add(child)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setSizing(widthType: Size, heightType: Size): T = apply {
        this.widthType = widthType
        this.heightType = heightType
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setSizing(width: Float, widthType: Size, height: Float, heightType: Size): T = apply {
        this.widthType = widthType
        this.heightType = heightType
        if (widthType == Size.Pixels) this.width = width else this.widthPercent = width
        if (heightType == Size.Pixels) this.height = height else this.heightPercent = height
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setPositioning(xConstraint: Pos, yConstraint: Pos): T = apply {
        this.xPositionConstraint = xConstraint
        this.yPositionConstraint = yConstraint
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setPositioning(xVal: Float, xPos: Pos, yVal: Float, yPos: Pos): T {
        this.xConstraint = xVal
        this.xPositionConstraint = xPos
        this.yConstraint = yVal
        this.yPositionConstraint = yPos
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun addTooltip(tooltip: String): T {
        tooltipElement = Tooltip().apply {
            innerText.text = tooltip
            childOf(this@CanvasElement)
        }
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun onMouseEnter(callback: (Float, Float) -> Unit): T = apply {
        mouseEnterListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onMouseExit(callback: (Float, Float) -> Unit): T = apply {
        mouseExitListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onMouseMove(callback: (Float, Float) -> Unit): T = apply {
        mouseMoveListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onHover(onEnter: (Float, Float) -> Unit, onExit: (Float, Float) -> Unit = { _, _ -> }): T = apply {
        onMouseEnter(onEnter)
        onMouseExit(onExit)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onMouseClick(callback: (Float, Float, Int) -> Boolean): T = apply {
        mouseClickListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onClick(callback: (Float, Float, Int) -> Boolean): T = apply {
        onMouseClick(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onMouseRelease(callback: (Float, Float, Int) -> Boolean): T = apply {
        mouseReleaseListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onRelease(callback: (Float, Float, Int) -> Boolean): T = apply {
        onMouseRelease(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onMouseScroll(callback: (Float, Float, Double, Double) -> Boolean): T = apply {
        mouseScrollListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onScroll(callback: (Float, Float, Double, Double) -> Boolean): T = apply {
        onMouseScroll(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onCharType(callback: (Int, Int, Char) -> Boolean): T = apply {
        charTypeListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onValueChange(callback: (Any) -> Unit): T = apply {
        this.onValueChange = callback
    } as T

    @Suppress("UNCHECKED_CAST")
    fun ignoreMouseEvents(): T = apply {
        mouseClickListeners.add { _, _, _ -> false }
        mouseReleaseListeners.add { _, _, _ -> false }
        mouseScrollListeners.add { _, _, _, _ -> false }
        mouseMoveListeners.add { _, _ -> }
        mouseEnterListeners.add { _, _ -> }
        mouseExitListeners.add { _, _ -> }
    } as T

    @Suppress("UNCHECKED_CAST")
    fun ignoreFocus(): T = apply {
        ignoreFocus = true
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setFloating(): T = apply {
        isFloating = true
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setRenderOnTop(): T = apply {
        renderOnTop = true
        parent?.let { p ->
            if (this in p.children) {
                p.children.remove(this)
                p.topChildren.add(this)
            }
        }
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setRequiresFocus(): T = apply {
        requiresFocus = true
    } as T

    @Suppress("UNCHECKED_CAST")
    fun show(): T = apply {
        visible = true
    } as T

    @Suppress("UNCHECKED_CAST")
    fun hide(): T = apply {
        visible = false
    } as T

    val hovered: Boolean get() = isHovered
    val pressed: Boolean get() = isPressed
    val focused: Boolean get() = isFocused
}