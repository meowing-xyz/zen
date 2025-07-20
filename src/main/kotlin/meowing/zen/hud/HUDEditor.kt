package meowing.zen.hud

import meowing.zen.hud.HUDManager.getScale
import meowing.zen.hud.HUDManager.getX
import meowing.zen.hud.HUDManager.getY
import meowing.zen.hud.HUDManager.isEnabled
import meowing.zen.hud.HUDManager.setPosition
import meowing.zen.utils.Utils
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
    private val undoStack = mutableListOf<Map<String, HUDPosition>>()
    private val redoStack = mutableListOf<Map<String, HUDPosition>>()
    private var dirty = false

    override fun initGui() {
        super.initGui()
        elements.clear()
        loadElements()
        saveState()
        dirty = false
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        if (dirty) {
            elements.forEach { element ->
                setPosition(element.name, element.targetX, element.targetY, element.scale, element.enabled)
            }
        }
    }

    private fun loadElements() {
        HUDManager.getElements().forEach { (name, text) ->
            val lines = text.split("\n")
            val width = lines.maxOfOrNull { mc.fontRendererObj.getStringWidth(it) } ?: 0
            val height = lines.size * mc.fontRendererObj.FONT_HEIGHT + 10
            elements.add(HUDElement(name, getX(name), getY(name), width + 10, height, text, getScale(name), isEnabled(name)))
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
            if (showToolbar) drawToolbar(actualMouseX, actualMouseY) else drawToolbarHint()
            if (showElements) drawElementList(actualMouseX, actualMouseY)
            if (showProperties) selected?.let { drawProperties(it) }
            drawTooltips()
        } else {
            drawPreviewHint()
        }

        GlStateManager.popMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)
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
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

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

        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    private fun drawToolbarHint() {
        val text = "Press T to toggle toolbar"
        val x = 15
        val y = 10
        drawRect(x - 5, y - 3, x + mc.fontRendererObj.getStringWidth(text) + 5, y + 13, Color(0, 0, 0, 180).rgb)
        mc.fontRendererObj.drawStringWithShadow(text, x.toFloat(), y.toFloat(), Color.WHITE.rgb)
    }

    private fun drawPreviewHint() {
        val text = "Press P to exit preview mode"
        val sr = ScaledResolution(mc)
        val x = (sr.scaledWidth - mc.fontRendererObj.getStringWidth(text)) / 2
        val y = 10
        drawRect(x - 5, y - 3, x + mc.fontRendererObj.getStringWidth(text) + 5, y + 13, Color(0, 0, 0, 180).rgb)
        mc.fontRendererObj.drawStringWithShadow(text, x.toFloat(), y.toFloat(), Color.WHITE.rgb)
    }

    private fun drawToolbar(mouseX: Int, mouseY: Int) {
        val sr = ScaledResolution(mc)
        val height = 30
        drawRect(0, 0, sr.scaledWidth, height, Color(20, 20, 30, 220).rgb)
        drawRect(0, height, sr.scaledWidth, height + 2, Color(70, 130, 180, 255).rgb)

        val buttons = listOf("Grid", "Snap", "Preview", "Reset", "Properties", "Elements")
        val states = listOf(showGrid, snapToGrid, previewMode, false, showProperties, showElements)
        var x = 15

        val title = "Zen - HUD Editor"
        val textWidth = mc.fontRendererObj.getStringWidth(title)
        val titlex = width - textWidth - 15f

        mc.fontRendererObj.drawStringWithShadow(title, titlex, 10f, Color(100, 180, 255).rgb)

        buttons.forEachIndexed { index, button ->
            val buttonWidth = mc.fontRendererObj.getStringWidth(button) + 20
            val hovered = mouseX in x..(x + buttonWidth) && mouseY in 0..height

            if (states[index]) {
                drawRect(x, height - 3, x + buttonWidth, height, Color(100, 180, 255).rgb)
            }

            val color = if (hovered) Color(100, 180, 255).rgb else Color(200, 220, 240).rgb
            mc.fontRendererObj.drawStringWithShadow(button, x + 10f, 10f, color)
            x += buttonWidth + 10
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

        mc.fontRendererObj.drawStringWithShadow("HUD Elements", listX + 10f, listY + 8f, Color(180, 220, 255).rgb)

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
            mc.fontRendererObj.drawString(displayName, listX + 10, elementY + 3, nameColor)

            val toggleText = if (element.enabled) "ON" else "OFF"
            val toggleColor = if (element.enabled) Color(100, 220, 100).rgb else Color(220, 100, 100).rgb
            mc.fontRendererObj.drawString(toggleText, listX + listWidth - 30, elementY + 3, toggleColor)
        }
    }

    private fun drawProperties(element: HUDElement) {
        val width = 140
        val height = 75
        val x = 15
        val y = this.height - height - 15

        drawRect(x, y, x + width, y + height, Color(20, 20, 30, 180).rgb)
        drawHollowRect(x, y, x + width, y + height, Color(70, 130, 180, 255).rgb)

        mc.fontRendererObj.drawStringWithShadow("Properties", x + 10f, y + 10f, Color(100, 180, 255).rgb)
        mc.fontRendererObj.drawStringWithShadow("Position: ${element.targetX.toInt()}, ${element.targetY.toInt()}", x + 15f, y + 25f, Color.WHITE.rgb)
        mc.fontRendererObj.drawStringWithShadow("Scale: ${"%.1f".format(element.scale)}", x + 15f, y + 40f, Color.WHITE.rgb)
        mc.fontRendererObj.drawStringWithShadow(if (element.enabled) "§aEnabled" else "§cDisabled", x + 15f, y + 55f, Color.WHITE.rgb)
    }

    private fun drawTooltips() {
        val tooltip = when {
            selected != null -> "Scroll to scale, Arrow keys to move"
            else -> null
        }

        tooltip?.let { text ->
            val sr = ScaledResolution(mc)
            val x = (sr.scaledWidth - mc.fontRendererObj.getStringWidth(text)) / 2
            val y = sr.scaledHeight - 30
            drawRect(x - 5, y - 3, x + mc.fontRendererObj.getStringWidth(text) + 5, y + 13, Color(0, 0, 0, 180).rgb)
            mc.fontRendererObj.drawStringWithShadow(text, x.toFloat(), y.toFloat(), Color(100, 180, 255).rgb)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val actualMouseX = Mouse.getX() * width / mc.displayWidth
        val actualMouseY = height - Mouse.getY() * height / mc.displayHeight - 1

        if (mouseButton == 0) {
            if ((!showToolbar || !handleToolbarClick(mouseX, mouseY)) && (!showElements || !handleElementListClick(actualMouseX, actualMouseY)))
                handleElementDrag(actualMouseX, actualMouseY)
        } else if (mouseButton == 1) {
            elements.reversed().find { it.isMouseOver(actualMouseX.toFloat(), actualMouseY.toFloat()) }?.let {
                selected = it
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun handleToolbarClick(mouseX: Int, mouseY: Int): Boolean {
        if (mouseY > 30) return false

        val buttons = listOf("Grid", "Snap", "Preview", "Reset", "Properties", "Elements", "Toolbar")
        var x = 15

        buttons.forEach { button ->
            val buttonWidth = mc.fontRendererObj.getStringWidth(button) + 20
            if (mouseX in x..(x + buttonWidth)) {
                when (button) {
                    "Grid" -> showGrid = !showGrid
                    "Snap" -> snapToGrid = !snapToGrid
                    "Preview" -> previewMode = !previewMode
                    "Reset" -> resetAll()
                    "Properties" -> showProperties = !showProperties
                    "Elements" -> showElements = !showElements
                    "Toolbar" -> showToolbar = !showToolbar
                }
                return true
            }
            x += buttonWidth + 10
        }
        return false
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
            dragOffsetX = mouseX - element.getRenderX(Utils.getPartialTicks())
            dragOffsetY = mouseY - element.getRenderY(Utils.getPartialTicks())
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
        when (keyCode) {
            Keyboard.KEY_ESCAPE -> {
                if (previewMode) previewMode = false
                else mc.displayGuiScreen(null)
                return
            }
            Keyboard.KEY_G -> showGrid = !showGrid
            Keyboard.KEY_P -> previewMode = !previewMode
            Keyboard.KEY_T -> showToolbar = !showToolbar
            Keyboard.KEY_R -> resetAll()
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