package meowing.zen.features.carrying

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.Zen.Companion.prefix
import meowing.zen.events.EventBus
import meowing.zen.events.GuiEvent
import meowing.zen.hud.HUDManager
import meowing.zen.utils.Render2D
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse

object CarryHudState {
    var hudX = 0f
    var hudY = 0f
    var hudScale = 1f
}

object CarryHUD {
    private const val name = "CarryHud"

    fun initialize() {
        HUDManager.register("CarryHud", "$prefix §f§lCarries:\n§7> §bPlayer1§f: §b5§f/§b10 §7(2.3s | 45/hr)\n§7> §bPlayer2§f: §b1§f/§b3 §7(15.7s | 32/hr)")
    }

    fun render() {
        if (CarryCounter.carryees.isEmpty() || Zen.isInInventory || !HUDManager.isEnabled(name)) return

        val x = HUDManager.getX(name)
        val y = HUDManager.getY(name)
        val scale = HUDManager.getScale(name)

        CarryHudState.hudX = x
        CarryHudState.hudY = y
        CarryHudState.hudScale = scale

        val lines = getLines()
        if (lines.isNotEmpty()) {
            var currentY = y
            val lineHeight = (mc.fontRendererObj.FONT_HEIGHT + 2) * scale
            for (line in lines) {
                Render2D.renderString(line, x, currentY, scale)
                currentY += lineHeight
            }
        }
    }

    private fun getLines(): List<String> {
        if (CarryCounter.carryees.isEmpty() || Zen.isInInventory) return emptyList()

        val lines = mutableListOf<String>()
        lines.add("$prefix §f§lCarries:")
        CarryCounter.carryees.mapTo(lines) {
            "§7> §b${it.name}§f: §b${it.count}§f/§b${it.total} §7(${it.getTimeSinceLastBoss()} | ${it.getBossPerHour()}§7)"
        }
        return lines
    }
}

object CarryInventoryHud {
    private data class Button(val x: Float, val y: Float, val width: Float, val height: Float, val action: String, val carryee: CarryCounter.Carryee, val tooltip: String)
    private data class RenderItem(val text: String, val x: Float, val y: Float, val color: Int, val shadow: Boolean)

    private val buttons = mutableListOf<Button>()
    private val renderItems = mutableListOf<RenderItem>()
    private var hoveredButton: Button? = null
    private var isRegistered = false
    private var guiClickHandler: EventBus.EventCall? = null
    private var guiDrawHandler: EventBus.EventCall? = null

    fun checkRegistration() {
        val shouldRegister = CarryCounter.carryees.isNotEmpty()
        if (shouldRegister != isRegistered) {
            try {
                if (shouldRegister) {
                    guiClickHandler = EventBus.register<GuiEvent.Click> ({ onMouseInput() })
                    guiDrawHandler = EventBus.register<GuiEvent.BackgroundDraw> ({ onGuiRender() })
                } else {
                    guiClickHandler?.unregister()
                    guiDrawHandler?.unregister()
                }
                isRegistered = shouldRegister
            } catch (e: Exception) {
                isRegistered = false
            }
        }
    }

    private fun onGuiRender() {
        if (CarryCounter.carryees.isEmpty() || !Zen.isInInventory || !HUDManager.isEnabled("CarryHud")) return
        buildRenderData()
        render()
    }

    private fun onMouseInput() {
        if (CarryCounter.carryees.isEmpty() || !Zen.isInInventory || !Mouse.getEventButtonState()) return
        val (mouseX, mouseY) = getMousePos()
        val button = buttons.find { mouseX in it.x..(it.x + it.width) && mouseY in it.y..(it.y + it.height) } ?: return
        when (button.action) {
            "add" -> if (button.carryee.count < button.carryee.total) button.carryee.count++
            "subtract" -> if (button.carryee.count > 0) button.carryee.count--
            "remove" -> {
                CarryCounter.removeCarryee(button.carryee.name)
                checkRegistration()
            }
        }
    }

    private fun getMousePos(): Pair<Float, Float> {
        val sr = ScaledResolution(mc)
        val mouseX = (Mouse.getX() * sr.scaledWidth / mc.displayWidth).toFloat()
        val mouseY = (sr.scaledHeight - Mouse.getY() * sr.scaledHeight / mc.displayHeight).toFloat()
        return mouseX to mouseY
    }

    private fun buildRenderData() {
        renderItems.clear()
        buttons.clear()

        val lineHeight = (mc.fontRendererObj.FONT_HEIGHT + 2) * CarryHudState.hudScale

        renderItems.add(RenderItem("$prefix §f§lCarries:", CarryHudState.hudX, CarryHudState.hudY, 0xFFFFFF, true))

        CarryCounter.carryees.forEachIndexed { i, carryee ->
            val y = CarryHudState.hudY + lineHeight + i * lineHeight
            val str = "§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} | ${carryee.getBossPerHour()}§7)"
            val x = CarryHudState.hudX + mc.fontRendererObj.getStringWidth(str) * CarryHudState.hudScale + 4 * CarryHudState.hudScale
            val buttonSpacing = 20 * CarryHudState.hudScale
            val buttonWidth = 18 * CarryHudState.hudScale
            val buttonHeight = 10 * CarryHudState.hudScale

            renderItems.add(RenderItem(str, CarryHudState.hudX, y, 0xFFFFFF, true))

            val buttonData = listOf(
                "add" to ("§a[+]" to "§aIncrease"),
                "subtract" to ("§c[-]" to "§cDecrease"),
                "remove" to ("§4[×]" to "§4Remove")
            )

            buttonData.forEachIndexed { j, (action, textTooltip) ->
                val (text, tooltip) = textTooltip
                val btnX = x + j * buttonSpacing
                buttons.add(Button(btnX, y, buttonWidth, buttonHeight, action, carryee, tooltip))
                renderItems.add(RenderItem(text, btnX, y, 0xAAAAAA, false))
            }
        }
    }

    private fun render() {
        val (mouseX, mouseY) = getMousePos()
        hoveredButton = buttons.find { mouseX in it.x..(it.x + it.width) && mouseY in it.y..(it.y + it.height) }

        renderItems.forEach { item ->
            val isHovered = hoveredButton?.let { btn -> btn.x == item.x && btn.y == item.y } == true
            val color = if (item.shadow || !isHovered) item.color else 0xFFFFFF
            Render2D.renderString(item.text, item.x, item.y, CarryHudState.hudScale, color)
        }

        renderTooltip(mouseX, mouseY)
    }

    private fun renderTooltip(mouseX: Float, mouseY: Float) {
        hoveredButton?.let { button ->
            val sr = ScaledResolution(mc)
            val tooltipWidth = mc.fontRendererObj.getStringWidth(button.tooltip) + 8
            val tooltipHeight = 16
            val tooltipX = (mouseX.toInt() - tooltipWidth / 2).coerceIn(2, sr.scaledWidth - tooltipWidth - 2)
            val tooltipY = (mouseY.toInt() - tooltipHeight - 8).coerceAtLeast(2)

            GlStateManager.pushMatrix()
            GlStateManager.translate(0f, 0f, 300f)
            GlStateManager.disableDepth()

            Gui.drawRect(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, 0xC8000000.toInt())
            Gui.drawRect(tooltipX - 1, tooltipY - 1, tooltipX + tooltipWidth + 1, tooltipY, 0xFF646464.toInt())
            Gui.drawRect(tooltipX - 1, tooltipY + tooltipHeight, tooltipX + tooltipWidth + 1, tooltipY + tooltipHeight + 1, 0xFF646464.toInt())
            Gui.drawRect(tooltipX - 1, tooltipY, tooltipX, tooltipY + tooltipHeight, 0xFF646464.toInt())
            Gui.drawRect(tooltipX + tooltipWidth, tooltipY, tooltipX + tooltipWidth + 1, tooltipY + tooltipHeight, 0xFF646464.toInt())

            Render2D.renderString(button.tooltip, tooltipX + 4f, tooltipY + 4f, 1f)

            GlStateManager.enableDepth()
            GlStateManager.popMatrix()
        }
    }
}