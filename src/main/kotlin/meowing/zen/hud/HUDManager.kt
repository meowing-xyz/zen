package meowing.zen.hud

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
import java.awt.Color

data class HUDPositions(
    val positions: MutableMap<String, HUDPosition> = mutableMapOf()
)

data class HUDPosition(var x: Float, var y: Float)

object HUDManager {
    private val registeredElements = mutableMapOf<String, String>()

    fun registerElement(name: String, exampleText: String) {
        registeredElements[name] = exampleText
    }

    fun getRegisteredElements(): Map<String, String> = registeredElements.toMap()
}

class HUDEditor : GuiScreen() {
    private val hudElements = mutableListOf<HUDElement>()
    private var draggedElement: HUDElement? = null
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var selectedElement: HUDElement? = null

    companion object {
        private val hudData = DataUtils("hud_positions", HUDPositions())

        fun getX(elementName: String): Float {
            return hudData.getData().positions[elementName]?.x ?: 10f
        }

        fun getY(elementName: String): Float {
            return hudData.getData().positions[elementName]?.y ?: 10f
        }

        fun setPosition(elementName: String, x: Float, y: Float) {
            val positions = hudData.getData().positions
            positions[elementName] = HUDPosition(x, y)
            hudData.save()
        }
    }

    override fun initGui() {
        super.initGui()
        hudElements.clear()

        var offsetY = 10f
        for ((name, exampleText) in HUDManager.getRegisteredElements()) {
            val lines = exampleText.split("\n")
            val maxWidth = lines.maxOfOrNull { mc.fontRendererObj.getStringWidth(it) } ?: 0
            val height = lines.size * mc.fontRendererObj.FONT_HEIGHT + 10
            hudElements.add(HUDElement(name, getX(name), getY(name), maxWidth + 10, height, exampleText))
            offsetY += 25f
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()

        val elementsToRender = hudElements.toList()
        for (element in elementsToRender) {
            element.render(mouseX.toFloat(), mouseY.toFloat(), Utils.getPartialTicks())
        }

        drawInstructions()
        GlStateManager.popMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            val elementsToCheck = hudElements.toList().reversed()
            for (element in elementsToCheck) {
                if (element.isMouseOver(mouseX.toFloat(), mouseY.toFloat())) {
                    draggedElement = element
                    selectedElement = element
                    dragOffsetX = mouseX - element.getRenderX(Utils.getPartialTicks())
                    dragOffsetY = mouseY - element.getRenderY(Utils.getPartialTicks())
                    break
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        draggedElement?.let { element ->
            val sr = ScaledResolution(mc)
            val newX = (mouseX - dragOffsetX).coerceIn(0f, (sr.scaledWidth - element.width).toFloat())
            val newY = (mouseY - dragOffsetY).coerceIn(0f, (sr.scaledHeight - element.height).toFloat())
            element.setPosition(newX, newY)
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        draggedElement?.let { element ->
            setPosition(element.name, element.targetX, element.targetY)
        }
        draggedElement = null
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == 1) {
            mc.displayGuiScreen(null)
        }
        super.keyTyped(typedChar, keyCode)
    }

    private fun drawInstructions() {
        val instructions = listOf(
            "Drag elements to move them",
            "Press ESC to exit"
        )

        var y = 5
        for (instruction in instructions) {
            mc.fontRendererObj.drawStringWithShadow(instruction, 5f, y.toFloat(), Color.WHITE.rgb)
            y += 12
        }
    }

    override fun doesGuiPauseGame(): Boolean = false
}

class HUDElement(val name: String, initialX: Float, initialY: Float, val width: Int, val height: Int, val exampleText: String) {
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
        val progress = (timeDiff * 10f).coerceIn(0f, 1f)
        return currentX + (targetX - currentX) * progress
    }

    fun getRenderY(partialTicks: Float): Float {
        val timeDiff = (System.currentTimeMillis() - lastUpdateTime) / 1000f
        val progress = (timeDiff * 10f).coerceIn(0f, 1f)
        return currentY + (targetY - currentY) * progress
    }

    fun render(mouseX: Float, mouseY: Float, partialTicks: Float) {
        val renderX = getRenderX(partialTicks)
        val renderY = getRenderY(partialTicks)
        val isHovered = isMouseOver(mouseX, mouseY)
        val alpha = if (isHovered) 120 else 80
        val borderColor = if (isHovered) Color.WHITE.rgb else Color.GRAY.rgb

        drawRect(renderX.toInt(), renderY.toInt(), (renderX + width).toInt(), (renderY + height).toInt(), Color(0, 0, 0, alpha).rgb)
        drawHollowRect(renderX.toInt(), renderY.toInt(), (renderX + width).toInt(), (renderY + height).toInt(), borderColor)

        val mc = Minecraft.getMinecraft()
        val lines = exampleText.split("\n")
        val startY = renderY + 5

        lines.forEachIndexed { index, line ->
            val textX = renderX + 5
            val textY = startY + (index * mc.fontRendererObj.FONT_HEIGHT)
            mc.fontRendererObj.drawStringWithShadow(line, textX, textY, Color.WHITE.rgb)
        }
    }

    fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        val renderX = getRenderX(Utils.getPartialTicks())
        val renderY = getRenderY(Utils.getPartialTicks())
        return mouseX >= renderX && mouseX <= renderX + width && mouseY >= renderY && mouseY <= renderY + height
    }

    private fun drawHollowRect(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        drawRect(x1, y1, x2, y1 + 1, color)
        drawRect(x1, y2 - 1, x2, y2, color)
        drawRect(x1, y1, x1 + 1, y2, color)
        drawRect(x2 - 1, y1, x2, y2, color)
    }
}

class HUDCommand : CommandBase() {
    override fun getCommandName(): String? {
        return "zenhud"
    }

    override fun getCommandUsage(sender: ICommandSender?): String? {
        return "/zenhud - Opens the Zen HUD Editor"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        if (sender is EntityPlayer)
            TickUtils.schedule(1) {
                Minecraft.getMinecraft().displayGuiScreen(HUDEditor())
            }
    }
}