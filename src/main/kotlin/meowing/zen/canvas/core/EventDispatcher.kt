package meowing.zen.canvas.core

import meowing.zen.Zen
import meowing.zen.events.EventBus
import meowing.zen.events.InternalEvent
import org.lwjgl.input.Mouse

@Zen.Module
object EventDispatcher {
    private val rootElements = mutableSetOf<CanvasElement<*>>()

    init {
        EventBus.register<InternalEvent.GuiMouse.Click> { event ->
            handleMouseClick(Mouse.getX().toFloat(), Mouse.getY().toFloat(), event.button)
        }

        EventBus.register<InternalEvent.GuiMouse.Release> { event ->
            handleMouseRelease(Mouse.getX().toFloat(), Mouse.getY().toFloat(), event.button)
        }

        EventBus.register<InternalEvent.GuiMouse.Move> { event ->
            handleMouseMove(Mouse.getX().toFloat(), Mouse.getY().toFloat())
        }

        EventBus.register<InternalEvent.GuiMouse.Scroll> { event ->
            (handleMouseScroll(Mouse.getX().toFloat(), Mouse.getY().toFloat(), event.horizontal, event.vertical))
        }

        EventBus.register<InternalEvent.GuiKey> { event ->
            if (handleCharPress(event.key, event.scanCode, event.character)) {
                event.cancel()
            }
        }
    }

    fun registerRoot(element: CanvasElement<*>) {
        rootElements.add(element)
    }

    fun unregisterRoot(element: CanvasElement<*>) {
        rootElements.remove(element)
    }

    private fun handleMouseClick(mouseX: Float, mouseY: Float, button: Int): Boolean {
        return rootElements.any { it.handleMouseClick(mouseX, mouseY, button) }
    }

    private fun handleMouseRelease(mouseX: Float, mouseY: Float, button: Int): Boolean {
        return rootElements.any { it.handleMouseRelease(mouseX, mouseY, button) }
    }

    private fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        return rootElements.any { it.handleMouseMove(mouseX, mouseY) }
    }

    private fun handleCharPress(keyCode: Int, scanCode: Int , charTyped: Char): Boolean {
        return rootElements.any { it.handleCharType(keyCode, scanCode, charTyped) }
    }

    private fun handleMouseScroll(mouseX: Float, mouseY: Float, horizontal: Double, vertical: Double): Boolean {
        return rootElements.any { it.handleMouseScroll(mouseX, mouseY, horizontal, vertical) }
    }
}