package meowing.zen.hud

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.UMatrixStack
import meowing.zen.utils.FontUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.*

class HUDEditor : GuiScreen() {
    private val fontObj = FontUtils.getFontRenderer()
    private val elements = mutableListOf<HUDElement>()
    private var dragging: HUDElement? = null
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var selected: HUDElement? = null
    private var showGrid = true
    private var snapToGrid = true
    private var gridSize = 10
    private var previewMode = false
    private var showProperties = true
    private var showElements = true
    private var showToolbar = true
    private var showResetConfirm = false
    private val undoStack = mutableListOf<Map<String, HUDPosition>>()
    private val redoStack = mutableListOf<Map<String, HUDPosition>>()
    private var dirty = false
    private var hoveredToolbarIndex = -1
    private val toolbarIcons = listOf(
        "/assets/zen/logos/HUDEditor/grid.png",
        "/assets/zen/logos/HUDEditor/snap.png",
        "/assets/zen/logos/HUDEditor/preview.png",
        "/assets/zen/logos/HUDEditor/props.png",
        "/assets/zen/logos/HUDEditor/list.png",
        "/assets/zen/logos/HUDEditor/reset.png"
    )
    private val toolbarTooltips = listOf(
        "Toggle Grid",
        "Toggle Snap to Grid",
        "Toggle Preview Mode",
        "Toggle Properties Panel",
        "Toggle Element List",
        "Reset All Elements"
    )
    private val window = Window(ElementaVersion.V10)
    private val toolbarContainer = UIContainer().apply {
        setX(10.pixels())
        setY(5.pixels())
        setWidth(400.pixels())
        setHeight(20.pixels())
    } childOf window

    override fun initGui() {
        super.initGui()
        elements.clear()
        loadElements()
        saveState()
        dirty = false
        setupToolbarIcons()
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        if (dirty) {
            elements.forEach { element ->
                HUDManager.setPosition(element.name, element.targetX, element.targetY, element.scale, element.enabled)
            }
        }
    }

    private fun setupToolbarIcons() {
        toolbarContainer.clearChildren()

        toolbarIcons.forEachIndexed { index, iconPath ->
            val iconContainer = UIContainer().apply {
                setX((index * 28).pixels())
                setY(0.pixels())
                setWidth(24.pixels())
                setHeight(20.pixels())
            } childOf toolbarContainer

            UIImage.ofResource(iconPath).apply {
                setX(CenterConstraint())
                setY(CenterConstraint())
                setWidth(16.pixels())
                setHeight(16.pixels())
            } childOf iconContainer

            iconContainer.onMouseEnter {
                hoveredToolbarIndex = index
            }

            iconContainer.onMouseLeave {
                hoveredToolbarIndex = -1
            }

            iconContainer.onMouseClick { event ->
                when (index) {
                    0 -> showGrid = !showGrid
                    1 -> snapToGrid = !snapToGrid
                    2 -> previewMode = !previewMode
                    3 -> showProperties = !showProperties
                    4 -> showElements = !showElements
                    5 -> showResetConfirm = true
                }
            }
        }
    }

    private fun loadElements() {
        HUDManager.getElements().forEach { (name, text) ->
            val lines = text.split("\n")
            val width = lines.maxOfOrNull { fontObj.getStringWidth(it) } ?: 0
            val height = lines.size * fontObj.FONT_HEIGHT + 10
            elements.add(HUDElement(name, HUDManager.getX(name), HUDManager.getY(name), width + 10, height, text, HUDManager.getScale(name), HUDManager.isEnabled(name)))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val actualMouseX = Mouse.getX() * width / mc.displayWidth
        val actualMouseY = height - Mouse.getY() * height / mc.displayHeight - 1

        drawBackground()
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()

        if (showGrid && !previewMode) drawGrid()
        elements.forEach {
            it.render(actualMouseX.toFloat(), actualMouseY.toFloat(), partialTicks, previewMode)
        }

        if (!previewMode) {
            if (showToolbar) {
                drawToolbarBackground()
                window.draw(UMatrixStack())
            } else {
                drawToolbarHint()
            }
            if (showElements) drawElementList(actualMouseX, actualMouseY)
            if (showProperties) selected?.let { drawProperties(it) }
            drawTooltips()
            if (hoveredToolbarIndex >= 0) drawToolbarTooltip(actualMouseX, actualMouseY, hoveredToolbarIndex)
        } else {
            drawPreviewHint()
        }

        if (showResetConfirm) {
            drawResetConfirmation(actualMouseX, actualMouseY)
        }

        GlStateManager.popMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawToolbarTooltip(mouseX: Int, mouseY: Int, index: Int) {
        val text = toolbarTooltips[index]
        val textWidth = fontObj.getStringWidth(text)
        val textHeight = fontObj.FONT_HEIGHT
        val padding = 4
        val tooltipWidth = textWidth + padding * 2
        val tooltipHeight = textHeight + padding * 2

        val sr = ScaledResolution(mc)
        var x = mouseX - tooltipWidth
        var y = mouseY - tooltipHeight

        x = x.coerceIn(0, sr.scaledWidth - tooltipWidth)
        y = y.coerceIn(0, sr.scaledHeight - tooltipHeight)

        drawRect(x, y, x + tooltipWidth, y + tooltipHeight, Color(30, 30, 40, 220).rgb)
        drawHollowRect(x, y, x + tooltipWidth, y + tooltipHeight, Color(100, 180, 255, 255).rgb)
        fontObj.drawStringWithShadow(text, (x + padding).toFloat(), (y + padding).toFloat(), Color.WHITE.rgb)
    }

    private fun drawBackground() {
        val sr = ScaledResolution(mc)
        drawGradientRect(0, 0, sr.scaledWidth, sr.scaledHeight, Color(15, 15, 25, 200).rgb, Color(25, 25, 35, 200).rgb)
    }

    private fun drawGrid() {
        val sr = ScaledResolution(mc)
        val r = 60
        val g = 60
        val b = 80
        val a = 100
        val tess = Tessellator.getInstance()
        val worldRenderer = tess.worldRenderer

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR)

        for (x in 0 until sr.scaledWidth step gridSize) {
            worldRenderer.pos(x.toDouble(), 0.0, 0.0).color(r, g, b, a).endVertex()
            worldRenderer.pos(x.toDouble(), sr.scaledHeight.toDouble(), 0.0).color(r, g, b, a).endVertex()
        }

        for (y in 0 until sr.scaledHeight step gridSize) {
            worldRenderer.pos(0.0, y.toDouble(), 0.0).color(r, g, b, a).endVertex()
            worldRenderer.pos(sr.scaledWidth.toDouble(), y.toDouble(), 0.0).color(r, g, b, a).endVertex()
        }

        tess.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    private fun drawToolbarHint() {
        val text = "Press T to toggle toolbar"
        val x = 15
        val y = 10
        drawRect(x - 5, y - 3, x + fontObj.getStringWidth(text) + 5, y + 13, Color(0, 0, 0, 180).rgb)
        fontObj.drawStringWithShadow(text, x.toFloat(), y.toFloat(), Color.WHITE.rgb)
    }

    private fun drawPreviewHint() {
        val text = "Press P to exit preview mode"
        val sr = ScaledResolution(mc)
        val x = (sr.scaledWidth - fontObj.getStringWidth(text)) / 2
        val y = 10
        drawRect(x - 5, y - 3, x + fontObj.getStringWidth(text) + 5, y + 13, Color(0, 0, 0, 180).rgb)
        fontObj.drawStringWithShadow(text, x.toFloat(), y.toFloat(), Color.WHITE.rgb)
    }

    private fun drawToolbarBackground() {
        val sr = ScaledResolution(mc)
        val height = 30
        drawRect(0, 0, sr.scaledWidth, height, Color(20, 20, 30, 220).rgb)
        drawRect(0, height, sr.scaledWidth, height + 2, Color(70, 130, 180, 255).rgb)

        val title = "Zen - HUD Editor"
        val textWidth = fontObj.getStringWidth(title)
        val titleX = width - textWidth - 15f
        fontObj.drawStringWithShadow(title, titleX, 10f, Color(100, 180, 255).rgb)

        val toolbarStates = listOf(showGrid, snapToGrid, previewMode, showProperties, showElements, false)
        toolbarStates.forEachIndexed { index, isActive ->
            if (isActive) {
                val x = 10 + (index * 28)
                drawRect(x, height - 3, x + 24, height, Color(100, 180, 255).rgb)
            }
        }
    }

    private fun drawElementList(mouseX: Int, mouseY: Int) {
        val sr = ScaledResolution(mc)
        val listWidth = 200
        val elementHeight = 16
        val headerHeight = 25
        val padding = 10
        val listHeight = minOf(elements.size * elementHeight + headerHeight + padding, sr.scaledHeight - 100)
        val listX = sr.scaledWidth - listWidth - 15
        val listY = if (showToolbar) 40 else 15

        drawRect(listX, listY, listX + listWidth, listY + listHeight, Color(20, 20, 30, 180).rgb)
        drawHollowRect(listX, listY, listX + listWidth, listY + listHeight, Color(70, 130, 180, 255).rgb)

        fontObj.drawStringWithShadow("HUD Elements", listX + 10f, listY + 8f, Color(180, 220, 255).rgb)

        val scrollOffset = if (elements.size * elementHeight > listHeight - headerHeight - padding) {
            maxOf(0, elements.size * elementHeight - (listHeight - headerHeight - padding))
        } else 0

        elements.forEachIndexed { index, element ->
            val elementY = listY + headerHeight + index * elementHeight - scrollOffset
            if (elementY < listY + headerHeight || elementY > listY + listHeight - elementHeight) return@forEachIndexed

            val isSelected = element == selected
            val isHovered = mouseX in listX..(listX + listWidth) && mouseY in elementY..(elementY + elementHeight)

            when {
                isSelected -> drawRect(listX + 5, elementY - 1, listX + listWidth - 5, elementY + elementHeight - 1, Color(40, 90, 140, 150).rgb)
                isHovered -> drawRect(listX + 5, elementY - 1, listX + listWidth - 5, elementY + elementHeight - 1, Color(50, 50, 70, 80).rgb)
            }

            val nameColor = if (element.enabled) Color(220, 240, 255).rgb else Color(150, 150, 170).rgb
            val displayName = element.name.take(17) + if (element.name.length > 17) "..." else ""
            fontObj.drawString(displayName, listX + 10, elementY + 3, nameColor)

            val toggleText = if (element.enabled) "ON" else "OFF"
            val toggleColor = if (element.enabled) Color(100, 220, 100).rgb else Color(220, 100, 100).rgb
            fontObj.drawString(toggleText, listX + listWidth - 30, elementY + 3, toggleColor)
        }
    }

    private fun drawProperties(element: HUDElement) {
        val width = 140
        val height = 75
        val x = 15
        val y = this.height - height - 15

        drawRect(x, y, x + width, y + height, Color(20, 20, 30, 180).rgb)
        drawHollowRect(x, y, x + width, y + height, Color(70, 130, 180, 255).rgb)

        fontObj.drawStringWithShadow("Properties", x + 10f, y + 10f, Color(100, 180, 255).rgb)
        fontObj.drawStringWithShadow("Position: ${element.targetX.toInt()}, ${element.targetY.toInt()}", x + 15f, y + 25f, Color.WHITE.rgb)
        fontObj.drawStringWithShadow("Scale: ${"%.1f".format(element.scale)}", x + 15f, y + 40f, Color.WHITE.rgb)
        fontObj.drawStringWithShadow(if (element.enabled) "§aEnabled" else "§cDisabled", x + 15f, y + 55f, Color.WHITE.rgb)
    }

    private fun drawResetConfirmation(mouseX: Int, mouseY: Int) {
        val sr = ScaledResolution(mc)
        val popupWidth = 280
        val popupHeight = 120
        val popupX = (sr.scaledWidth - popupWidth) / 2
        val popupY = (sr.scaledHeight - popupHeight) / 2

        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 0f, 300f)

        drawRect(0, 0, sr.scaledWidth, sr.scaledHeight, Color(0, 0, 0, 120).rgb)
        drawRect(popupX, popupY, popupX + popupWidth, popupY + popupHeight, Color(25, 25, 35, 240).rgb)
        drawHollowRect(popupX, popupY, popupX + popupWidth, popupY + popupHeight, Color(70, 130, 180, 255).rgb)

        val titleText = "Reset All Elements"
        val titleX = popupX + (popupWidth - fontObj.getStringWidth(titleText)) / 2
        fontObj.drawStringWithShadow(titleText, titleX.toFloat(), popupY + 15f, Color(220, 100, 100).rgb)

        val messageText = "This will reset all HUD elements to"
        val messageText2 = "default positions and enable them."
        val messageX = popupX + (popupWidth - fontObj.getStringWidth(messageText)) / 2
        val messageX2 = popupX + (popupWidth - fontObj.getStringWidth(messageText2)) / 2
        fontObj.drawString(messageText, messageX, popupY + 40, Color(200, 200, 200).rgb)
        fontObj.drawString(messageText2, messageX2, popupY + 55, Color(200, 200, 200).rgb)

        val buttonWidth = 80
        val buttonHeight = 20
        val buttonSpacing = 20
        val confirmX = popupX + (popupWidth / 2) - buttonWidth - (buttonSpacing / 2)
        val cancelX = popupX + (popupWidth / 2) + (buttonSpacing / 2)
        val buttonY = popupY + popupHeight - 35

        val confirmHovered = mouseX in confirmX..(confirmX + buttonWidth) && mouseY in buttonY..(buttonY + buttonHeight)
        val cancelHovered = mouseX in cancelX..(cancelX + buttonWidth) && mouseY in buttonY..(buttonY + buttonHeight)

        val confirmBg = if (confirmHovered) Color(200, 80, 80, 200).rgb else Color(170, 60, 60, 180).rgb
        val cancelBg = if (cancelHovered) Color(60, 120, 180, 200).rgb else Color(40, 100, 160, 180).rgb

        drawRect(confirmX, buttonY, confirmX + buttonWidth, buttonY + buttonHeight, confirmBg)
        drawRect(cancelX, buttonY, cancelX + buttonWidth, buttonY + buttonHeight, cancelBg)

        drawHollowRect(confirmX, buttonY, confirmX + buttonWidth, buttonY + buttonHeight, Color(255, 120, 120, 255).rgb)
        drawHollowRect(cancelX, buttonY, cancelX + buttonWidth, buttonY + buttonHeight, Color(120, 180, 255, 255).rgb)

        val confirmText = "Reset"
        val cancelText = "Cancel"
        val confirmTextX = confirmX + (buttonWidth - fontObj.getStringWidth(confirmText)) / 2
        val cancelTextX = cancelX + (buttonWidth - fontObj.getStringWidth(cancelText)) / 2
        val textY = buttonY + 6

        fontObj.drawStringWithShadow(confirmText, confirmTextX.toFloat(), textY.toFloat(), Color.WHITE.rgb)
        fontObj.drawStringWithShadow(cancelText, cancelTextX.toFloat(), textY.toFloat(), Color.WHITE.rgb)

        GlStateManager.popMatrix()
    }

    private fun drawTooltips() {
        val tooltip = when {
            selected != null -> "Scroll to scale, Arrow keys to move"
            else -> null
        }

        tooltip?.let { text ->
            val sr = ScaledResolution(mc)
            val x = (sr.scaledWidth - fontObj.getStringWidth(text)) / 2
            val y = sr.scaledHeight - 30
            drawRect(x - 5, y - 3, x + fontObj.getStringWidth(text) + 5, y + 13, Color(0, 0, 0, 180).rgb)
            fontObj.drawStringWithShadow(text, x.toFloat(), y.toFloat(), Color(100, 180, 255).rgb)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val actualMouseX = Mouse.getX() * width / mc.displayWidth
        val actualMouseY = height - Mouse.getY() * height / mc.displayHeight - 1

        if (showResetConfirm) {
            handleResetConfirmClick(actualMouseX, actualMouseY)
            return
        }

        if (showToolbar) {
            window.mouseClick(mouseX.toDouble(), mouseY.toDouble(), mouseButton)
        }

        if (mouseButton == 0) {
            if ((!showToolbar || !handleToolbarClick(mouseY)) && (!showElements || !handleElementListClick(actualMouseX, actualMouseY)))
                handleElementDrag(actualMouseX, actualMouseY)
        } else if (mouseButton == 1) {
            elements.reversed().find { it.isMouseOver(actualMouseX.toFloat(), actualMouseY.toFloat()) }?.let {
                selected = it
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun handleResetConfirmClick(mouseX: Int, mouseY: Int) {
        val sr = ScaledResolution(mc)
        val popupWidth = 280
        val popupHeight = 120
        val popupX = (sr.scaledWidth - popupWidth) / 2
        val popupY = (sr.scaledHeight - popupHeight) / 2

        val buttonWidth = 80
        val buttonHeight = 20
        val buttonSpacing = 20
        val confirmX = popupX + (popupWidth / 2) - buttonWidth - (buttonSpacing / 2)
        val cancelX = popupX + (popupWidth / 2) + (buttonSpacing / 2)
        val buttonY = popupY + popupHeight - 35

        when {
            mouseX in confirmX..(confirmX + buttonWidth) && mouseY in buttonY..(buttonY + buttonHeight) -> {
                resetAll()
                showResetConfirm = false
            }
            mouseX in cancelX..(cancelX + buttonWidth) && mouseY in buttonY..(buttonY + buttonHeight) -> {
                showResetConfirm = false
            }
            mouseX !in popupX..(popupX + popupWidth) || mouseY !in popupY..(popupY + popupHeight) -> {
                showResetConfirm = false
            }
        }
    }

    private fun handleToolbarClick(mouseY: Int): Boolean {
        return mouseY <= 30
    }

    private fun handleElementListClick(mouseX: Int, mouseY: Int): Boolean {
        val sr = ScaledResolution(mc)
        val listWidth = 200
        val listX = sr.scaledWidth - listWidth - 15
        val listY = if (showToolbar) 40 else 15
        val elementHeight = 16
        val headerHeight = 25

        if (mouseX !in listX..(listX + listWidth)) return false

        val clickedIndex = (mouseY - listY - headerHeight) / elementHeight
        if (clickedIndex !in 0 until elements.size) return false

        val element = elements[clickedIndex]
        if (mouseX >= listX + listWidth - 40) {
            element.enabled = !element.enabled
            dirty = true
        } else {
            selected = element
        }
        return true
    }

    private fun handleElementDrag(mouseX: Int, mouseY: Int) {
        if (previewMode) return

        elements.reversed().find { it.isMouseOver(mouseX.toFloat(), mouseY.toFloat()) }?.let { element ->
            dragging = element
            selected = element
            dragOffsetX = mouseX - element.getRenderX()
            dragOffsetY = mouseY - element.getRenderY()
            saveState()
        }
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        val actualMouseX = Mouse.getX() * width / mc.displayWidth
        val actualMouseY = height - Mouse.getY() * height / mc.displayHeight - 1

        dragging?.let { element ->
            val sr = ScaledResolution(mc)
            var newX = actualMouseX - dragOffsetX
            var newY = actualMouseY - dragOffsetY

            if (snapToGrid) {
                newX = (newX / gridSize).roundToInt() * gridSize.toFloat()
                newY = (newY / gridSize).roundToInt() * gridSize.toFloat()
            }

            newX = newX.coerceIn(0f, (sr.scaledWidth - element.width).toFloat())
            newY = newY.coerceIn(0f, (sr.scaledHeight - element.height).toFloat())

            element.setPosition(newX, newY)
            dirty = true
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (showToolbar) {
            window.mouseRelease()
        }

        dragging?.let { element ->
            dragging = null
            dirty = true
        }
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()

        val dWheel = Mouse.getEventDWheel()
        if (dWheel != 0 && selected != null) {
            saveState()
            val scaleDelta = if (dWheel > 0) 0.1f else -0.1f
            selected!!.scale = (selected!!.scale + scaleDelta).coerceIn(0.2f, 5f)
            dirty = true
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (showResetConfirm) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_N) {
                showResetConfirm = false
            } else if (keyCode == Keyboard.KEY_Y || keyCode == Keyboard.KEY_RETURN) {
                resetAll()
                showResetConfirm = false
            }
            return
        }

        when (keyCode) {
            Keyboard.KEY_ESCAPE -> {
                if (previewMode) previewMode = false
                else mc.displayGuiScreen(null)
                return
            }
            Keyboard.KEY_G -> showGrid = !showGrid
            Keyboard.KEY_P -> previewMode = !previewMode
            Keyboard.KEY_T -> showToolbar = !showToolbar
            Keyboard.KEY_R -> showResetConfirm = true
            Keyboard.KEY_Z -> if (isCtrlKeyDown()) undo()
            Keyboard.KEY_Y -> if (isCtrlKeyDown()) redo()
            Keyboard.KEY_DELETE -> selected?.let { delete(it) }
            Keyboard.KEY_UP -> selected?.let { move(it, 0, -1) }
            Keyboard.KEY_DOWN -> selected?.let { move(it, 0, 1) }
            Keyboard.KEY_LEFT -> selected?.let { move(it, -1, 0) }
            Keyboard.KEY_RIGHT -> selected?.let { move(it, 1, 0) }
            Keyboard.KEY_EQUALS, Keyboard.KEY_ADD -> selected?.let { scale(it, 0.1f) }
            Keyboard.KEY_MINUS, Keyboard.KEY_SUBTRACT -> selected?.let { scale(it, -0.1f) }
        }
        super.keyTyped(typedChar, keyCode)
    }

    private fun scale(element: HUDElement, delta: Float) {
        saveState()
        element.scale = (element.scale + delta).coerceIn(0.2f, 5f)
        dirty = true
    }

    private fun move(element: HUDElement, deltaX: Int, deltaY: Int) {
        saveState()
        val moveAmount = if (isShiftKeyDown()) 10 else 1
        val newX = element.targetX + deltaX * moveAmount
        val newY = element.targetY + deltaY * moveAmount

        val sr = ScaledResolution(mc)
        val clampedX = newX.coerceIn(0f, (sr.scaledWidth - element.width).toFloat())
        val clampedY = newY.coerceIn(0f, (sr.scaledHeight - element.height).toFloat())

        element.setPosition(clampedX, clampedY)
        dirty = true
    }

    private fun delete(element: HUDElement) {
        saveState()
        element.enabled = false
        selected = null
        dirty = true
    }

    private fun resetAll() {
        saveState()
        elements.forEach { element ->
            element.setPosition(50f, 50f)
            element.enabled = true
            element.scale = 1f
        }
        dirty = true
    }

    private fun saveState() {
        val state = elements.associate { it.name to HUDPosition(it.targetX, it.targetY, it.scale, it.enabled) }
        undoStack.add(state)
        if (undoStack.size > 20) undoStack.removeFirst()
        redoStack.clear()
    }

    private fun undo() {
        if (undoStack.size > 1) {
            redoStack.add(undoStack.removeLast())
            applyState(undoStack.last())
            dirty = true
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val state = redoStack.removeLast()
            undoStack.add(state)
            applyState(state)
            dirty = true
        }
    }

    private fun applyState(state: Map<String, HUDPosition>) {
        elements.forEach { element ->
            state[element.name]?.let { pos ->
                element.setPosition(pos.x, pos.y)
                element.scale = pos.scale
                element.enabled = pos.enabled
            }
        }
    }

    private fun drawHollowRect(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        drawRect(x1, y1, x2, y1 + 1, color)
        drawRect(x1, y2 - 1, x2, y2, color)
        drawRect(x1, y1, x1 + 1, y2, color)
        drawRect(x2 - 1, y1, x2, y2, color)
    }

    override fun doesGuiPauseGame() = false
}