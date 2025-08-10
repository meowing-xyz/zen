package meowing.zen.config.ui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.Utils.createBlock
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import java.awt.Color

class MultiCheckboxElement(
    private val options: List<String> = emptyList(),
    initialSelected: Set<Int> = emptySet(),
    private val onChange: ((Set<Int>) -> Unit)? = null
) : UIContainer() {

    companion object {
        var openMultiCheckboxElement: MultiCheckboxElement? = null
        fun closeAllMultiCheckboxes() = openMultiCheckboxElement?.collapse()
    }

    private var selectedIndices = initialSelected.toMutableSet()
    private val normalBg = Color(18, 24, 28, 255)
    private val hoverBg = Color(25, 35, 40, 255)
    private val selectedBg = Color(40, 80, 90, 255)
    private val textColor = Color(170, 230, 240, 255)

    private var titleText: UIWrappedText
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

        titleText = (UIWrappedText(getDisplayText(), centered = true).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = mc.fontRendererObj.getStringWidth(getDisplayText()).pixels()
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

    private fun getDisplayText(): String = "${selectedIndices.size} selected"

    private fun updateDisplayText() {
        val newText = getDisplayText()
        titleText.setText(newText)
        titleText.setWidth(mc.fontRendererObj.getStringWidth(newText).pixels)
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
                if (isExpanded && !isMouseOverMultiCheckbox()) {
                    findScrollComponentUnderMouse()?.mouseScroll(event.delta)
                }
            } childOf window) as UIContainer?
        } catch (e: Exception) {
            println("Failed to create click interceptor: $e")
        }
    }

    private fun isClickInContainer(x: Float, y: Float) = isClickInBounds(x, y, container)

    private fun isClickInOptions(x: Float, y: Float) = optionsContainer?.let { isClickInBounds(x, y, it) } == true

    private fun isClickInBounds(x: Float, y: Float, component: UIComponent) =
        x >= component.getLeft() && x <= component.getRight() && y >= component.getTop() && y <= component.getBottom()

    private fun isMouseOverMultiCheckbox(): Boolean {
        val (mouseX, mouseY) = getScaledMousePos()
        return isClickInBounds(mouseX, mouseY, container) || (optionsContainer?.let { isClickInBounds(mouseX, mouseY, it) } == true)
    }

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

        closeAllMultiCheckboxes()
        openMultiCheckboxElement = this
        isExpanded = true

        container.setColor(selectedBg)
        createClickInterceptor()

        val expandedHeight = options.size * (container.getHeight() + 2)
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
        openMultiCheckboxElement = null

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
                println("Failed to remove click interceptor: $e")
            }
        }
        clickInterceptor = null
    }

    private fun createOptions() {
        var yOffset = 2f

        options.forEachIndexed { index, option ->
            val optionComponent = createBlock(3f).constrain {
                x = 0.pixels()
                y = yOffset.pixels()
                width = 100.percent()
                height = container.getHeight().pixels
            }.setColor(normalBg) childOf optionsContainer!!

            yOffset += container.getHeight() + 2f

            val checkboxBox = createBlock(3f).constrain {
                x = 4.pixels()
                y = CenterConstraint()
                width = 12.pixels()
                height = 12.pixels()
            }.setColor(if (selectedIndices.contains(index)) selectedBg else normalBg.darker()) childOf optionComponent

            UIWrappedText(option).constrain {
                x = 20.pixels()
                y = CenterConstraint()
                textScale = 0.8.pixels()
                width = (100.percent() - 40.pixels())
            }.setColor(textColor) childOf optionComponent

            optionComponent.onMouseClick { event ->
                event.stopPropagation()
                toggleOption(index, checkboxBox)
            }

            optionComponent.onMouseEnter {
                optionComponent.animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, hoverBg.toConstraint()) }
            }

            optionComponent.onMouseLeave {
                optionComponent.animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, normalBg.toConstraint()) }
            }
        }
    }

    private fun toggleOption(index: Int, checkboxBox: UIComponent) {
        if (selectedIndices.contains(index)) {
            selectedIndices.remove(index)
            checkboxBox.animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, normalBg.darker().toConstraint()) }
        } else {
            selectedIndices.add(index)
            checkboxBox.animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, selectedBg.toConstraint()) }
        }

        updateDisplayText()
        onChange?.invoke(selectedIndices.toSet())
    }
}