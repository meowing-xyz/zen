package meowing.zen.canvas.core

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EventBus
import meowing.zen.events.GuiEvent
import meowing.zen.events.KeyEvent
import meowing.zen.events.MouseEvent
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

@Zen.Module
object EventDispatcher {
    private val rootElements = mutableSetOf<CanvasElement<*>>()

    init {
        EventBus.register<GuiEvent.Mouse.Press> { event ->
            handleMouseClick(Utils.MouseX, Utils.MouseY, event.mouseButton)
        }

        EventBus.register<GuiEvent.Mouse.Release> { event ->
            handleMouseRelease(Utils.MouseX, Utils.MouseY, event.mouseButton)
        }

        EventBus.register<GuiEvent.Mouse.Move> {
            handleMouseMove(Utils.MouseX, Utils.MouseY)
        }

        // TODO: Impl
//        EventBus.register<MouseEvent.Scroll> { event ->
//            (handleMouseScroll(Mouse.getX().toFloat(), Mouse.getY().toFloat(), event.horizontal, event.vertical))
//        }
//
        EventBus.register<KeyEvent.Press> { event ->
            handleKeyPress(event.keyCode, 0, 0)
        }

        EventBus.register<KeyEvent.Release> { event ->
            handleKeyRelease(event.keyCode, 0, 0)
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

    private fun handleKeyPress(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return rootElements.any { it.handleKeyPress(keyCode, scanCode, modifiers) }
    }

    private fun handleKeyRelease(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return rootElements.any { it.handleKeyRelease(keyCode, scanCode, modifiers) }
    }

    private fun handleMouseScroll(mouseX: Float, mouseY: Float, horizontal: Double, vertical: Double): Boolean {
        return rootElements.any { it.handleMouseScroll(mouseX, mouseY, horizontal, vertical) }
    }
}