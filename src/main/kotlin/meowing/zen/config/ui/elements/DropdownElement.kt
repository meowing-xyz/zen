package meowing.zen.config.ui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import meowing.zen.Zen.Companion.LOGGER
import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.createBlock
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.min

class DropdownElement(
    private val options: List<String> = emptyList(),
    initialSelected: Int = 0,
    private val onChange: ((Int) -> Unit)? = null
) : UIContainer() {

    companion object {
        var openDropdownElement: DropdownElement? = null
        fun closeAllDropdowns() = openDropdownElement?.collapse()
    }

    private var selectedIndex = min(initialSelected, options.size - 1)
    private val normalBg = Color(18, 24, 28, 255)
    private val hoverBg = Color(25, 35, 40, 255)
    private val selectedBg = Color(40, 80, 90, 255)
    private val textColor = Color(170, 230, 240, 255)

    private var selectedText: UIWrappedText
    private var container: UIComponent
    private var optionsContainer: UIContainer? = null
    private var clickInterceptor: UIContainer? = null
    private var isExpanded = false

    init {
        container = createBlock(3f).constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = 100.percent()
            height = 100.percent()
        }.setColor(normalBg) childOf this

        selectedText = (UIWrappedText(options.getOrNull(selectedIndex) ?: "", centered = true).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = mc.fontRendererObj.getStringWidth(options.getOrNull(selectedIndex) ?: "").pixels()
        }.setColor(textColor) childOf container) as UIWrappedText

        container.onMouseClick { event ->
            event.stopPropagation()
            if (isExpanded) collapse() else expand()
        }

        container.onMouseEnter {
            if (!isExpanded) container.animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, hoverBg.toConstraint()) }
        }

        container.onMouseLeave {
            if (!isExpanded) container.animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, normalBg.toConstraint()) }
        }
    }

    private fun createClickInterceptor() {
        if (clickInterceptor != null) return

        try {
            val window = Window.of(this)
            clickInterceptor = (UIContainer().constrain {
                x = 0.pixels()
                y = 0.pixels()
                width = 100.percent()
                height = 100.percent()
            }.onMouseClick { event ->
                if (isExpanded) {
                    val clickX = event.absoluteX
                    val clickY = event.absoluteY

                    if (isClickInContainer(clickX, clickY)) {
                        collapse()
                    } else if (isClickInOptions(clickX, clickY)) {
                        optionsContainer?.children?.find { isClickInBounds(clickX, clickY, it) }?.mouseClick(clickX.toDouble(), clickY.toDouble(), event.mouseButton)
                    } else {
                        collapse()
                    }
                }
            }.onMouseScroll { event ->
                if (isExpanded) findScrollComponentUnderMouse()?.mouseScroll(event.delta)
            } childOf window) as UIContainer?
        } catch (e: Exception) {
            LOGGER.warn("Failed to create click interceptor: $e")
        }
    }

    private fun isClickInContainer(x: Float, y: Float) = isClickInBounds(x, y, container)

    private fun isClickInOptions(x: Float, y: Float) = optionsContainer?.let { isClickInBounds(x, y, it) } == true

    private fun isClickInBounds(x: Float, y: Float, component: UIComponent) =
        x >= component.getLeft() && x <= component.getRight() && y >= component.getTop() && y <= component.getBottom()

    private fun getScaledMousePos(): Pair<Float, Float> {
        val scaledResolution = ScaledResolution(mc)
        val mouseX = Mouse.getX() * scaledResolution.scaledWidth / mc.displayWidth
        val mouseY = scaledResolution.scaledHeight - Mouse.getY() * scaledResolution.scaledHeight / mc.displayHeight - 1
        return mouseX.toFloat() to mouseY.toFloat()
    }

    private fun findScrollComponentUnderMouse(): UIComponent? {
        val (mouseX, mouseY) = getScaledMousePos()
        return findScrollComponents(Window.of(this)).find { isClickInBounds(mouseX, mouseY, it) }
    }

    private fun findScrollComponents(component: UIComponent): List<UIComponent> =
        mutableListOf<UIComponent>().apply {
            if (component.javaClass.simpleName.contains("ScrollComponent")) add(component)
            component.children.forEach { addAll(findScrollComponents(it)) }
        }

    private fun expand() {
        if (isExpanded) return

        closeAllDropdowns()
        openDropdownElement = this
        isExpanded = true

        container.setColor(selectedBg)
        createClickInterceptor()

        val expandedHeight = (options.size - 1) * (container.getHeight() + 2)
        (parent.parent as? UIContainer)?.animate {
            setHeightAnimation(Animations.OUT_QUAD, 0.2f, (48 + expandedHeight).pixels())
        }

        optionsContainer = UIContainer().constrain {
            x = 0.pixels()
            y = 100.percent()
            width = 100.percent()
            height = expandedHeight.pixels()
        } childOf this

        createOptions()
    }

    private fun collapse() {
        if (!isExpanded) return

        isExpanded = false
        openDropdownElement = null

        container.animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, normalBg.toConstraint()) }

        (parent.parent as? UIContainer)?.animate {
            setHeightAnimation(Animations.OUT_QUAD, 0.2f, 48.pixels())
        }

        optionsContainer?.let { removeChild(it) }
        optionsContainer = null

        clickInterceptor?.let {
            try {
                Window.of(this).removeChild(it)
            } catch (e: Exception) {
                LOGGER.warn("Failed to remove click interceptor: $e")
            }
        }
        clickInterceptor = null
    }

    private fun createOptions() {
        var yOffset = 2f

        options.forEachIndexed { index, option ->
            if (index == selectedIndex) return@forEachIndexed

            val optionComponent = createBlock(3f).constrain {
                x = 0.pixels()
                y = yOffset.pixels()
                width = 100.percent()
                height = container.getHeight().pixels
            }.setColor(normalBg) childOf optionsContainer!!

            yOffset += container.getHeight() + 2f

            optionComponent.onMouseClick { event ->
                event.stopPropagation()
                selectOption(index)
            }

            optionComponent.onMouseEnter {
                optionComponent.animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, hoverBg.toConstraint()) }
            }

            optionComponent.onMouseLeave {
                optionComponent.animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, normalBg.toConstraint()) }
            }

            UIWrappedText(option, centered = true).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                textScale = 0.8.pixels()
                width = mc.fontRendererObj.getStringWidth(option).pixels()
            }.setColor(textColor) childOf optionComponent
        }
    }

    private fun selectOption(index: Int) {
        selectedIndex = index
        selectedText.setText(options[index])
        selectedText.setWidth(mc.fontRendererObj.getStringWidth(options[index]).pixels)
        onChange?.invoke(index)
        collapse()
    }
}