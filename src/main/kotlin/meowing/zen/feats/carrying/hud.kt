package meowing.zen.feats.carrying

import cc.polyfrost.oneconfig.hud.TextHud
import meowing.zen.Zen
import meowing.zen.utils.LoopUtils.loop
import meowing.zen.events.EventBus
import meowing.zen.events.GuiBackgroundDrawEvent
import meowing.zen.events.GuiClickEvent
import meowing.zen.events.RenderWorldEvent
import org.lwjgl.input.Mouse
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager

class CarryHud : TextHud(true, 10, 100) {
    override fun getLines(lines: MutableList<String>, example: Boolean) {
        if (example) {
            lines.add("§c[Zen] §f§lCarries:")
            lines.add("§7> §bPlayer1§f: §b5§f/§b10 §7(2.3s | 45/hr)")
            lines.add("§7> §bPlayer2§f: §b1§f/§b3 §7(15.7s | 32/hr)")
            return
        }
        if (carrycounter.carryees.isEmpty() || Zen.Companion.isInInventory) return
        lines.add("§c[Zen] §f§lCarries:")
        carrycounter.carryees.mapTo(lines) {
            "§7> §b${it.name}§f: §b${it.count}§f/§b${it.total} §7(${it.getTimeSinceLastBoss()} | ${it.getBossPerHour()}§7)"
        }
    }
}

object CarryInventoryHud {
    private data class Button(val x: Float, val y: Float, val width: Float, val height: Float, val action: String, val carryee: carrycounter.Carryee, val tooltip: String)
    private data class RenderItem(val text: String, val x: Float, val y: Float, val color: Int, val shadow: Boolean)

    private val buttons = mutableListOf<Button>()
    private val renderItems = mutableListOf<RenderItem>()
    private var hoveredButton: Button? = null
    private var mouseX = 0f
    private var mouseY = 0f
    private var isRegistered = false
    private var cachedSR: ScaledResolution? = null
    private var lastScreenSize = 0

    private var guiClickHandler: EventBus.EventCall? = null
    private var guiDrawHandler: EventBus.EventCall? = null

    fun checkRegistration() {
        val shouldRegister = carrycounter.carryees.isNotEmpty()
        if (shouldRegister != isRegistered) {
            try {
                if (shouldRegister) {
                    guiClickHandler = EventBus.register<GuiClickEvent> ({ onMouseInput() })
                    guiDrawHandler = EventBus.register<GuiBackgroundDrawEvent> ({ onGuiRender() })
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
        if (carrycounter.carryees.isEmpty() || !Zen.Companion.isInInventory) return
        updateMousePos()
        buildRenderData()
        render()
    }

    private fun onMouseInput() {
        if (carrycounter.carryees.isEmpty() || !Zen.Companion.isInInventory || !Mouse.getEventButtonState()) return
        updateMousePos()
        buttons.find { mouseX in it.x..(it.x + it.width) && mouseY in it.y..(it.y + it.height) }?.let { button ->
            when (button.action) {
                "add" -> if (button.carryee.count < button.carryee.total) button.carryee.count++
                "subtract" -> if (button.carryee.count > 0) button.carryee.count--
                "remove" -> {
                    carrycounter.removeCarryee(button.carryee.name)
                    checkRegistration()
                }
            }
        }
    }

    private fun updateMousePos() {
        val mc = Minecraft.getMinecraft()
        val screenSize = mc.displayWidth + mc.displayHeight
        if (cachedSR == null || screenSize != lastScreenSize) {
            cachedSR = ScaledResolution(mc)
            lastScreenSize = screenSize
        }
        val sr = cachedSR!!
        mouseX = (Mouse.getX() * sr.scaledWidth / mc.displayWidth).toFloat()
        mouseY = (sr.scaledHeight - Mouse.getY() * sr.scaledHeight / mc.displayHeight).toFloat()
    }

    private fun buildRenderData() {
        val mc = Minecraft.getMinecraft()
        renderItems.clear()
        buttons.clear()
        renderItems.add(RenderItem("§c[Zen] §f§lCarries:", 10f, 104f, 0xFFFFFF, true))

        carrycounter.carryees.forEachIndexed { i, carryee ->
            val y = 116f + i * 12
            val str = "§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} | ${carryee.getBossPerHour()}§7)"
            val x = 10f + mc.fontRendererObj.getStringWidth(str) + 4
            renderItems.add(RenderItem(str, 10f, y, 0xFFFFFF, true))
            listOf("add" to "§a[+]", "subtract" to "§c[-]", "remove" to "§4[×]").forEachIndexed { j, (action, text) ->
                val btnX = x + j * 20
                buttons.add(Button(btnX, y, 18f, 10f, action, carryee, when(action) { "add" -> "§aIncrease" "subtract" -> "§cDecrease" else -> "§4Remove" }))
                renderItems.add(RenderItem(text, btnX, y, 0xAAAAAA, false))
            }
        }
    }

    private fun render() {
        val mc = Minecraft.getMinecraft()
        hoveredButton = buttons.find { mouseX in it.x..(it.x + it.width) && mouseY in it.y..(it.y + it.height) }
        renderItems.forEach {
            val color = if (it.shadow || hoveredButton?.let { btn -> btn.x == it.x && btn.y == it.y } != true) it.color else 0xFFFFFF
            mc.fontRendererObj.drawString(it.text, it.x, it.y, color, it.shadow)
        }
        renderTooltip()
    }

    private fun renderTooltip() {
        hoveredButton?.let { button ->
            val mc = Minecraft.getMinecraft()
            val sr = cachedSR ?: return
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
            mc.fontRendererObj.drawString(button.tooltip, (tooltipX + 4).toFloat(), (tooltipY + 4).toFloat(), 0xFFFFFF, false)
            GlStateManager.enableDepth()
            GlStateManager.popMatrix()
        }
    }
}