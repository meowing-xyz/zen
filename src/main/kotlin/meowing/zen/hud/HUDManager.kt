package meowing.zen.hud

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.DataUtils
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui.drawRect
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.*

data class HUDPosition(var x: Float, var y: Float, var scale: Float = 1f, var enabled: Boolean = true)
data class HUDPositions(val positions: MutableMap<String, HUDPosition> = mutableMapOf())

object HUDManager {
    private val elements = mutableMapOf<String, String>()
    private val categories = mutableMapOf<String, String>()

    fun register(name: String, exampleText: String, category: String = "General") {
        elements[name] = exampleText
        categories[name] = category
    }

    fun getElements(): Map<String, String> = elements
    fun getCategory(name: String): String = categories[name] ?: "General"
    fun getCategories(): Set<String> = categories.values.toSet()
}

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
    private var scaling: HUDElement? = null
    private var initialScale = 1f
    private var initialMouseY = 0
    private val undoStack = mutableListOf<Map<String, HUDPosition>>()
    private val redoStack = mutableListOf<Map<String, HUDPosition>>()

    companion object {
        private val hudData = DataUtils("hud_positions", HUDPositions())

        fun getX(name: String): Float = hudData.getData().positions[name]?.x ?: 10f
        fun getY(name: String): Float = hudData.getData().positions[name]?.y ?: 10f
        fun getScale(name: String): Float = hudData.getData().positions[name]?.scale ?: 1f
        fun isEnabled(name: String): Boolean = hudData.getData().positions[name]?.enabled ?: true

        fun setPosition(name: String, x: Float, y: Float, scale: Float = 1f, enabled: Boolean = true) {
            hudData.getData().positions[name] = HUDPosition(x, y, scale, enabled)
            hudData.save()
        }

        fun toggle(name: String) {
            val positions = hudData.getData().positions
            val current = positions[name] ?: HUDPosition(10f, 10f)
            positions[name] = current.copy(enabled = !current.enabled)
            hudData.save()
        }
    }

    override fun initGui() {
        super.initGui()
        elements.clear()
        loadElements()
        saveState()
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
        elements.forEach { it.render(actualMouseX.toFloat(), actualMouseY.toFloat(), partialTicks, previewMode) }

        if (!previewMode) {
            drawToolbar(actualMouseX, actualMouseY)
            drawElementList(actualMouseX, actualMouseY)
            selected?.let { drawProperties(it) }
            drawScaleTooltip()
        } else {
            drawPreviewHint()
        }

        GlStateManager.popMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawBackground() {
        val sr = ScaledResolution(mc)
        drawGradientRect(
            0,
            0,
            sr.scaledWidth,
            sr.scaledHeight,
            Color(15, 15, 25, 200).rgb,
            Color(25, 25, 35, 200).rgb
        )
    }

    private fun drawGrid() {
        val sr = ScaledResolution(mc)
        val color = Color(60, 60, 80, 100).rgb

        for (x in 0 until sr.scaledWidth step gridSize) {
            drawRect(x, 0, x + 1, sr.scaledHeight, color)
        }
        for (y in 0 until sr.scaledHeight step gridSize) {
            drawRect(0, y, sr.scaledWidth, y + 1, color)
        }
    }

    private fun drawPreviewHint() {
        val text = "Press P to exit preview mode"
        val width = mc.fontRendererObj.getStringWidth(text)
        val sr = ScaledResolution(mc)
        val x = (sr.scaledWidth - width) / 2
        val y = 10

        drawRect(x - 5, y - 3, x + width + 5, y + 13, Color(0, 0, 0, 180).rgb)
        mc.fontRendererObj.drawStringWithShadow(text, x.toFloat(), y.toFloat(), Color.WHITE.rgb)
    }

    private fun drawToolbar(mouseX: Int, mouseY: Int) {
        val sr = ScaledResolution(mc)
        val height = 30

        drawRect(0, 0, sr.scaledWidth, height, Color(20, 20, 30, 220).rgb)
        drawRect(0, height, sr.scaledWidth, height + 2, Color(70, 130, 180, 255).rgb)

        val buttons = listOf("Grid", "Snap", "Preview", "Reset")
        val states = listOf(showGrid, snapToGrid, previewMode, false)

        var x = 15
        buttons.forEachIndexed { index, button ->
            val buttonWidth = mc.fontRendererObj.getStringWidth(button) + 20
            val hovered = mouseX in x..(x + buttonWidth) && mouseY in 0..height

            if (states[index]) {
                drawRect(x, height - 3, x + buttonWidth, height, Color(100, 180, 255).rgb)
            }

            val color = if (hovered) Color(100, 180, 255).rgb else Color(200, 220, 240).rgb
            mc.fontRendererObj.drawStringWithShadow(button, x + 10f, 8f, color)

            x += buttonWidth + 10
        }
    }

    private fun drawElementList(mouseX: Int, mouseY: Int) {
        val sr = ScaledResolution(mc)
        val listWidth = 200
        val listHeight = sr.scaledHeight - 100
        val listX = sr.scaledWidth - listWidth - 15
        val listY = 40

        drawRect(listX, listY, listX + listWidth, listY + listHeight, Color(20, 20, 30, 180).rgb)
        drawHollowRect(listX, listY, listX + listWidth, listY + listHeight, Color(70, 130, 180, 255).rgb)

        mc.fontRendererObj.drawStringWithShadow("HUD Elements", listX + 10f, listY + 8f, Color(180, 220, 255).rgb)

        elements.forEachIndexed { index, element ->
            val elementY = listY + 30 + index * 16
            val isSelected = element == selected
            val isHovered = mouseX in listX..(listX + listWidth) && mouseY in elementY..(elementY + 15)

            when {
                isSelected -> drawRect(listX + 5, elementY - 1, listX + listWidth - 5, elementY + 14, Color(40, 90, 140, 150).rgb)
                isHovered -> drawRect(listX + 5, elementY - 1, listX + listWidth - 5, elementY + 14, Color(50, 50, 70, 80).rgb)
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
        val width = 180
        val height = 90
        val x = 15
        val y = this.height - height - 15

        drawRect(x, y, x + width, y + height, Color(20, 20, 30, 180).rgb)
        drawHollowRect(x, y, x + width, y + height, Color(70, 130, 180, 255).rgb)

        mc.fontRendererObj.drawStringWithShadow("Properties", x + 10f, y + 10f, Color(100, 180, 255).rgb)
        mc.fontRendererObj.drawStringWithShadow("X: ${element.targetX.toInt()}", x + 15f, y + 25f, Color.WHITE.rgb)
        mc.fontRendererObj.drawStringWithShadow("Y: ${element.targetY.toInt()}", x + 15f, y + 40f, Color.WHITE.rgb)
        mc.fontRendererObj.drawStringWithShadow("Scale: ${"%.1f".format(element.scale)}", x + 15f, y + 55f, Color.WHITE.rgb)
        mc.fontRendererObj.drawStringWithShadow("Enabled: ${element.enabled}", x + 15f, y + 70f, Color.WHITE.rgb)
    }

    private fun drawScaleTooltip() {
        when {
            scaling != null -> {
                val text = "Release mouse to set scale"
                val x = Mouse.getX() * width / mc.displayWidth
                val y = height - Mouse.getY() * height / mc.displayHeight - 30
                val textWidth = mc.fontRendererObj.getStringWidth(text)

                drawRect(x - 5, y - 3, x + textWidth + 5, y + 13, Color(0, 0, 0, 180).rgb)
                mc.fontRendererObj.drawStringWithShadow(text, x.toFloat(), y.toFloat(), Color(100, 180, 255).rgb)
            }
            Keyboard.isKeyDown(Keyboard.KEY_S) && selected != null -> {
                val text = "Drag to scale element"
                val sr = ScaledResolution(mc)
                val x = (sr.scaledWidth - mc.fontRendererObj.getStringWidth(text)) / 2
                val y = sr.scaledHeight - 30

                drawRect(x - 5, y - 3, x + mc.fontRendererObj.getStringWidth(text) + 5, y + 13, Color(0, 0, 0, 180).rgb)
                mc.fontRendererObj.drawStringWithShadow(text, x.toFloat(), y.toFloat(), Color(100, 180, 255).rgb)
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val actualMouseX = Mouse.getX() * width / mc.displayWidth
        val actualMouseY = height - Mouse.getY() * height / mc.displayHeight - 1

        if (mouseButton == 0) {
            if (Keyboard.isKeyDown(Keyboard.KEY_S) && selected != null) {
                scaling = selected
                initialScale = scaling!!.scale
                initialMouseY = actualMouseY
                return
            }

            handleToolbarClick(actualMouseX, actualMouseY)
            handleElementListClick(actualMouseX, actualMouseY)
            handleElementDrag(actualMouseX, actualMouseY)
        } else if (mouseButton == 1) {
            elements.reversed().find { it.isMouseOver(actualMouseX.toFloat(), actualMouseY.toFloat()) }?.let {
                selected = it
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun handleToolbarClick(mouseX: Int, mouseY: Int) {
        if (mouseY > 30) return

        val buttons = listOf("Grid", "Snap", "Preview", "Reset")
        var x = 15

        buttons.forEach { button ->
            val buttonWidth = mc.fontRendererObj.getStringWidth(button) + 20
            if (mouseX in x..(x + buttonWidth)) {
                when (button) {
                    "Grid" -> showGrid = !showGrid
                    "Snap" -> snapToGrid = !snapToGrid
                    "Preview" -> previewMode = !previewMode
                    "Reset" -> resetAll()
                }
                return
            }
            x += buttonWidth + 10
        }
    }

    private fun handleElementListClick(mouseX: Int, mouseY: Int) {
        val sr = ScaledResolution(mc)
        val listWidth = 200
        val listX = sr.scaledWidth - listWidth - 15
        val listY = 40

        if (mouseX !in listX..(listX + listWidth)) return

        val clickedIndex = (mouseY - listY - 30) / 16
        if (clickedIndex !in 0 until elements.size) return

        val element = elements[clickedIndex]

        if (mouseX >= listX + listWidth - 40) {
            element.enabled = !element.enabled
            setPosition(element.name, element.targetX, element.targetY, element.scale, element.enabled)
        } else {
            selected = element
        }
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

        scaling?.let { element ->
            val scaleDelta = (initialMouseY - actualMouseY) * 0.01f
            element.scale = (initialScale + scaleDelta).coerceIn(0.5f, 3f)
            return
        }

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
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        scaling?.let { element ->
            setPosition(element.name, element.targetX, element.targetY, element.scale, element.enabled)
            scaling = null
        }

        dragging?.let { element ->
            setPosition(element.name, element.targetX, element.targetY, element.scale, element.enabled)
        }
        dragging = null
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        when (keyCode) {
            Keyboard.KEY_ESCAPE -> {
                scaling = null
                mc.displayGuiScreen(null)
            }
            Keyboard.KEY_G -> showGrid = !showGrid
            Keyboard.KEY_P -> previewMode = !previewMode
            Keyboard.KEY_R -> resetAll()
            Keyboard.KEY_Z -> if (isCtrlKeyDown()) undo()
            Keyboard.KEY_Y -> if (isCtrlKeyDown()) redo()
            Keyboard.KEY_DELETE -> selected?.let { delete(it) }
            Keyboard.KEY_UP -> selected?.let { move(it, 0, -1) }
            Keyboard.KEY_DOWN -> selected?.let { move(it, 0, 1) }
            Keyboard.KEY_LEFT -> selected?.let { move(it, -1, 0) }
            Keyboard.KEY_RIGHT -> selected?.let { move(it, 1, 0) }
        }
        super.keyTyped(typedChar, keyCode)
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
        setPosition(element.name, clampedX, clampedY, element.scale, element.enabled)
    }

    private fun delete(element: HUDElement) {
        saveState()
        element.enabled = false
        setPosition(element.name, element.targetX, element.targetY, element.scale, false)
        selected = null
    }

    private fun resetAll() {
        saveState()
        elements.forEach { element ->
            element.setPosition(100f, 100f)
            element.enabled = true
            element.scale = 1f
            setPosition(element.name, 10f, 10f, 1f, true)
        }
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
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val state = redoStack.removeLast()
            undoStack.add(state)
            applyState(state)
        }
    }

    private fun applyState(state: Map<String, HUDPosition>) {
        elements.forEach { element ->
            state[element.name]?.let { pos ->
                element.setPosition(pos.x, pos.y)
                element.scale = pos.scale
                element.enabled = pos.enabled
                setPosition(element.name, pos.x, pos.y, pos.scale, pos.enabled)
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

class HUDElement(
    val name: String,
    initialX: Float,
    initialY: Float,
    val width: Int,
    val height: Int,
    val exampleText: String,
    var scale: Float = 1f,
    var enabled: Boolean = true
) {
    private var currentX = initialX
    private var currentY = initialY
    var targetX = initialX
    var targetY = initialY
    private var lastUpdateTime = System.currentTimeMillis()

    fun setPosition(x: Float, y: Float) {
        currentX = getRenderX(Utils.getPartialTicks())
        currentY = getRenderY(Utils.getPartialTicks())
        targetX = x
        targetY = y
        lastUpdateTime = System.currentTimeMillis()
    }

    fun getRenderX(partialTicks: Float): Float {
        val timeDiff = (System.currentTimeMillis() - lastUpdateTime) / 1000f
        val progress = (timeDiff * 8f).coerceIn(0f, 1f)
        return currentX + (targetX - currentX) * easeOutCubic(progress)
    }

    fun getRenderY(partialTicks: Float): Float {
        val timeDiff = (System.currentTimeMillis() - lastUpdateTime) / 1000f
        val progress = (timeDiff * 8f).coerceIn(0f, 1f)
        return currentY + (targetY - currentY) * easeOutCubic(progress)
    }

    private fun easeOutCubic(t: Float) = 1f + (t - 1f).pow(3)

    fun render(mouseX: Float, mouseY: Float, partialTicks: Float, previewMode: Boolean) {
        if (!enabled && previewMode) return

        val renderX = getRenderX(partialTicks)
        val renderY = getRenderY(partialTicks)
        val isHovered = isMouseOver(mouseX, mouseY)

        GlStateManager.pushMatrix()
        GlStateManager.translate(renderX + width / 2, renderY + height / 2, 0f)
        GlStateManager.scale(scale, scale, 1f)
        GlStateManager.translate(-width / 2.0, -height / 2.0, 0.0)

        if (!previewMode) {
            val alpha = if (!enabled) 40 else if (isHovered) 140 else 90
            val borderColor = when {
                !enabled -> Color(200, 60, 60).rgb
                isHovered -> Color(100, 180, 255).rgb
                else -> Color(100, 100, 120).rgb
            }

            drawRect(0, 0, width, height, Color(30, 35, 45, alpha).rgb)
            drawHollowRect(0, 0, width, height, borderColor)
        }

        val lines = exampleText.split("\n")
        val textAlpha = if (enabled) 255 else 128
        val textColor = Color(220, 240, 255, textAlpha).rgb

        lines.forEachIndexed { index, line ->
            val textY = 5f + (index * mc.fontRendererObj.FONT_HEIGHT)
            mc.fontRendererObj.drawStringWithShadow(line, 5f, textY, textColor)
        }

        GlStateManager.popMatrix()
    }

    fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        val renderX = getRenderX(Utils.getPartialTicks())
        val renderY = getRenderY(Utils.getPartialTicks())
        val scaledWidth = width * scale
        val scaledHeight = height * scale
        val offsetX = (width - scaledWidth) / 2
        val offsetY = (height - scaledHeight) / 2

        return mouseX >= renderX + offsetX && mouseX <= renderX + offsetX + scaledWidth && mouseY >= renderY + offsetY && mouseY <= renderY + offsetY + scaledHeight
    }

    private fun drawHollowRect(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        drawRect(x1, y1, x2, y1 + 1, color)
        drawRect(x1, y2 - 1, x2, y2, color)
        drawRect(x1, y1, x1 + 1, y2, color)
        drawRect(x2 - 1, y1, x2, y2, color)
    }
}

class HUDCommand : CommandBase() {
    override fun getCommandName() = "zenhud"
    override fun getCommandUsage(sender: ICommandSender?) = "/zenhud - Opens the Zen HUD Editor"
    override fun getRequiredPermissionLevel() = 0

    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        if (sender is EntityPlayer) {
            TickUtils.schedule(1) {
                mc.displayGuiScreen(HUDEditor())
            }
        }
    }
}