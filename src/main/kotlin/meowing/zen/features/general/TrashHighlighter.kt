package meowing.zen.features.general

import com.google.gson.reflect.TypeToken
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.UKeyboard
import gg.essential.universal.UMatrixStack
import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.constraint.ChildHeightConstraint
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.GuiEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.DataUtils
import meowing.zen.utils.ItemUtils.skyblockID
import meowing.zen.utils.TickUtils
import net.minecraft.client.gui.Gui
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.client.config.GuiUtils
import java.awt.Color

enum class FilterType { REGEX, EQUALS, CONTAINS }
enum class InputType { ITEM_ID, DISPLAY_NAME }

@Zen.Module
object TrashHighlighter : Feature("trashhighlighter") {
    private val highlightType by ConfigDelegate<Double>("trashhighlighttype")
    private val color by ConfigDelegate<Color>("trashhighlightercolor")
    private val defaultList = listOf(
        FilteredItem("CRYPT_DREADLORD_SWORD", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("MACHINE_GUN_BOW", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("Healing VIII", FilterType.CONTAINS, InputType.DISPLAY_NAME),
        FilteredItem("DUNGEON_LORE_PAPER", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("ENCHANTED_BONE", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("CRYPT_BOW", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("(?:SKELETON|ZOMBIE)_(?:GRUNT|MASTER|SOLDIER)_(?:BOOTS|LEGGINGS|CHESTPLATE|HELMET)", FilterType.REGEX, InputType.ITEM_ID),
        FilteredItem("SUPER_HEAVY", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("INFLATABLE_JERRY", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("DUNGEON_TRAP", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("SNIPER_HELMET", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("SKELETOR", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("ROTTEN", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("HEAVY", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("PREMIUM_FLESH", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("TRAINING", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("CONJURING_SWORD", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("FEL_PEARL", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("ZOMBIE_KNIGHT", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("ENCHANTED_ROTTEN_FLESH", FilterType.CONTAINS, InputType.ITEM_ID)
    )
    val trashFilters = DataUtils("trashFilters", defaultList.toMutableList(), object : TypeToken<MutableList<FilteredItem>>() {})

    data class FilteredItem(
        var textInput: String,
        val selectedFilter: FilterType,
        val selectedInput: InputType
    ) {
        fun matches(stack: ItemStack): Boolean {
            val input = when (selectedInput) {
                InputType.ITEM_ID -> stack.skyblockID
                InputType.DISPLAY_NAME -> stack.displayName
            }

            return when (selectedFilter) {
                FilterType.CONTAINS -> input.contains(textInput)
                FilterType.EQUALS -> input == textInput
                FilterType.REGEX -> try { input.matches(textInput.toRegex()) } catch (_: Exception) { false }
            }
        }
    }

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Trash Highlighter", ConfigElement(
                "trashhighlighter",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Trash Highlighter", "Color", ConfigElement(
                "trashhighlightercolor",
                "Highlight color",
                ElementType.ColorPicker(Color(255, 0, 0, 127))
            ))
            .addElement("General", "Trash Highlighter", "Type", ConfigElement(
                "trashhighlighttype",
                "Highlight type",
                ElementType.Dropdown(listOf("Slot", "Border"), 0)
            ))
            .addElement("General", "Trash Highlighter", "GUI", ConfigElement(
                "trashhighlightguibutton",
                "Trash Highlighter Filter GUI",
                ElementType.Button("Open Filter GUI") { _, _ ->
                    TickUtils.schedule(2) {
                        mc.displayGuiScreen(TrashFilterGui())
                    }
                }
            ))
    }

    override fun initialize() {
        register<GuiEvent.Slot.RenderPost> { event ->
            if (!event.slot.hasStack) return@register
            val stack = event.slot.stack ?: return@register
            val x = event.slot.xDisplayPosition
            val y = event.slot.yDisplayPosition

            if (stack.skyblockID.isNotEmpty() && isTrashItem(stack)) {
                val highlightColor = color.rgb

                when (highlightType.toInt()) {
                    0 -> Gui.drawRect(x, y, x + 16, y + 16, highlightColor)
                    1 -> {
                        Gui.drawRect(x, y, x + 16, y + 1, highlightColor)
                        Gui.drawRect(x, y, x + 1, y + 16, highlightColor)
                        Gui.drawRect(x + 15, y, x + 16, y + 16, highlightColor)
                        Gui.drawRect(x, y + 15, x + 16, y + 16, highlightColor)
                    }
                }
            }
        }
    }

    private fun safeGetFilters(): List<FilteredItem> {
        return try {
            trashFilters.getData()
        } catch (e: Exception) {
            println("[Zen] Error in Trash Highlighter//getFilter: $e")
            emptyList()
        }
    }

    private fun isTrashItem(stack: ItemStack): Boolean {
        return safeGetFilters().any { filter ->
            try { filter.matches(stack) } catch (_: Exception) { false }
        }
    }

    fun getFilters(): List<FilteredItem> = safeGetFilters()
    fun setFilters(filters: List<FilteredItem>) = trashFilters.setData(filters.toMutableList())
    fun resetToDefault() = trashFilters.setData(defaultList.toMutableList())
}

class TextInput(
    initialValue: String = "",
    placeholder: String = "",
    var onChange: ((String) -> Unit)? = null
) : UIContainer() {
    var text: String = initialValue
    val input: UITextInput
    private val placeholderText: UIText?
    private var onInputCallback: ((String) -> Unit)? = null

    init {
        val container = UIRoundedRectangle(3f).constrain {
            x = 1.pixels()
            y = 1.pixels()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        }.setColor(Color(18, 24, 28, 255)) childOf this

        input = (UITextInput(text).constrain {
            x = 8.pixels()
            y = CenterConstraint()
            width = 100.percent() - 16.pixels()
            height = 10.pixels()
        }.setColor(Color(170, 230, 240, 255)) childOf container) as UITextInput

        placeholderText = (if (placeholder.isNotEmpty()) {
            UIText(placeholder).constrain {
                x = 8.pixels()
                y = CenterConstraint()
            }.setColor(Color(80, 120, 140, 255)) childOf container
        } else null) as UIText?

        updatePlaceholderVisibility()
        setupEventHandlers()
    }

    private fun setupEventHandlers() {
        onMouseClick {
            input.setText(text)
            input.grabWindowFocus()
        }

        input.onKeyType { _, _ ->
            text = input.getText()
            updatePlaceholderVisibility()
            onChange?.invoke(text)
            onInputCallback?.invoke(text)
        }

        input.onFocusLost {
            text = input.getText()
            onChange?.invoke(text)
        }
    }

    private fun updatePlaceholderVisibility() {
        placeholderText?.let { placeholder ->
            if (text.isEmpty()) placeholder.unhide(true)
            else placeholder.hide(true)
        }
    }
}

class TrashFilterGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    private val theme = object {
        val bg = Color(8, 12, 16, 255)
        val element = Color(12, 16, 20, 255)
        val accent = Color(100, 245, 255, 255)
        val accent2 = Color(80, 200, 220, 255)
        val success = Color(47, 102, 47, 255)
        val danger = Color(115, 41, 41, 255)
        val buttonGroup = Color(16, 20, 24, 255)
        val buttonSelected = Color(70, 180, 200, 255)
        val buttonHover = Color(20, 70, 75, 255)
        val divider = Color(30, 35, 40, 255)
    }

    private val tooltipElements: MutableMap<UIComponent, Set<String>> = mutableMapOf()
    private lateinit var scrollComponent: ScrollComponent
    private lateinit var listContainer: UIContainer
    private lateinit var inputField: TextInput

    init {
        buildGui()
    }

    override fun onScreenClose() {
        super.onScreenClose()
        TrashHighlighter.trashFilters.save()
    }

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)
        tooltipElements.entries.forEach { (element, tooltip) ->
            if (element.isHovered()) {
                GuiUtils.drawHoveringText(
                    tooltip.toMutableList(),
                    mouseX, mouseY,
                    window.getWidth().toInt(),
                    window.getHeight().toInt(),
                    -1, mc.fontRendererObj
                )
            }
        }
    }

    private fun UIComponent.addTooltip(tooltip: Set<String>) {
        tooltipElements[this] = tooltip
    }

    private fun createBlock(radius: Float): UIRoundedRectangle = UIRoundedRectangle(radius)

    private fun buildGui() {
        val border = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 70.percent()
            height = 80.percent()
        }.setColor(theme.accent2) childOf window

        val main = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
        }.setColor(theme.bg) childOf border

        createHeader(main)
        createContent(main)
        createFooter(main)
        renderFilters()
    }

    private fun createHeader(parent: UIComponent) {
        val header = UIContainer().constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 40.pixels()
        } childOf parent

        UIText("§lTrash Filter").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.8.pixels()
        }.setColor(theme.accent) childOf header

        val resetButton = createBlock(3f).constrain {
            x = 8.pixels(true)
            y = CenterConstraint()
            width = 24.pixels()
            height = 24.pixels()
        }.setColor(theme.element) childOf header

        resetButton.onMouseEnter {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, Color.RED.darker().toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.element.toConstraint()) }
        }.onMouseClick {
            TrashHighlighter.resetToDefault()
            renderFilters()
            ChatUtils.addMessage("§aReset filters to default!")
        }

        resetButton.addTooltip(setOf("§c§lReset to Default", "§7Warning: Clears all filters"))

        UIText("⟲").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.2.pixels()
        }.setColor(theme.accent) childOf resetButton

        createBlock(0f).constrain {
            x = 0.percent()
            y = 100.percent() - 1.pixels()
            width = 100.percent()
            height = 1.pixels()
        }.setColor(theme.accent2) childOf header
    }

    private fun createContent(parent: UIComponent) {
        val contentPanel = UIContainer().constrain {
            x = 8.pixels()
            y = 48.pixels()
            width = 100.percent() - 16.pixels()
            height = 100.percent() - 96.pixels()
        } childOf parent

        scrollComponent = ScrollComponent().constrain {
            x = 4.pixels()
            y = 4.pixels()
            width = 100.percent() - 8.pixels()
            height = 100.percent() - 8.pixels()
        } childOf contentPanel

        listContainer = UIContainer().constrain {
            width = 100.percent()
            height = ChildHeightConstraint(4f)
        } childOf scrollComponent
    }

    private fun createFooter(parent: UIComponent) {
        val footer = UIContainer().constrain {
            x = 8.pixels()
            y = 100.percent() - 40.pixels()
            width = 100.percent() - 16.pixels()
            height = 40.pixels()
        } childOf parent

        createBlock(0f).constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 1.pixels()
        }.setColor(theme.accent2) childOf footer

        inputField = TextInput("", "Enter filter pattern...").constrain {
            x = 8.pixels()
            y = CenterConstraint()
            width = 100.percent() - 80.pixels()
            height = 24.pixels()
        } childOf footer

        inputField.input.onKeyType { _, keyCode ->
            if (keyCode == UKeyboard.KEY_ENTER) addFilter()
        }

        val addButton = createBlock(3f).constrain {
            x = 100.percent() - 64.pixels()
            y = CenterConstraint()
            width = 56.pixels()
            height = 24.pixels()
        }.setColor(theme.element) childOf footer

        addButton.onMouseEnter {
            if(inputField.text.isEmpty()) {
                animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.danger.toConstraint()) }
                return@onMouseEnter
            }
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.success.toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.element.toConstraint()) }
        }.onMouseClick {
            addFilter()
            inputField.input.setText("")
        }

        UIText("Add").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(theme.accent) childOf addButton
    }

    private fun renderFilters() {
        listContainer.clearChildren()
        val filters = TrashHighlighter.getFilters()

        if (filters.isEmpty()) {
            UIText("No filters added...").constrain {
                x = CenterConstraint()
                y = 20.pixels()
                textScale = 1f.pixels()
            }.setColor(theme.accent2.withAlpha(128)) childOf listContainer
            return
        }

        filters.forEachIndexed { index, filter ->
            createFilterRow(index, filter)
        }
    }

    private fun createFilterRow(index: Int, filter: TrashHighlighter.FilteredItem) {
        val row = createBlock(3f).constrain {
            x = 0.percent()
            y = CramSiblingConstraint(4f)
            width = 100.percent()
            height = 36.pixels()
        }.setColor(theme.element) childOf listContainer

        val textInput = TextInput(filter.textInput).constrain {
            x = 8.pixels()
            y = CenterConstraint()
            width = 45.5.percent()
            height = 24.pixels()
        } childOf row

        textInput.onChange = { text ->
            updateFilter(index, text, filter.selectedFilter, filter.selectedInput)
        }

        createInputTypeButtons(row, index, filter)
        createFilterTypeButtons(row, index, filter)
        createDeleteButton(row, index)
    }

    private fun createInputTypeButtons(parent: UIComponent, index: Int, filter: TrashHighlighter.FilteredItem) {
        val container = createBlock(2f).constrain {
            x = 48.percent()
            y = CenterConstraint()
            width = 18.percent()
            height = 20.pixels()
        }.setColor(theme.buttonGroup) childOf parent

        val options = listOf(
            InputType.ITEM_ID to "ID",
            InputType.DISPLAY_NAME to "Name"
        )

        options.forEachIndexed { optionIndex, (type, name) ->
            createButtonGroupOption(
                name,
                filter.selectedInput == type,
                optionIndex == 0,
                optionIndex == options.size - 1
            ) {
                updateFilter(index, filter.textInput, filter.selectedFilter, type)
            }.constrain {
                x = (optionIndex * 50).percent()
                y = 0.percent()
                width = 50.percent()
                height = 100.percent()
            } childOf container

            if (optionIndex < options.size - 1) {
                createBlock(0f).constrain {
                    x = ((optionIndex + 1) * 50).percent()
                    y = 2.pixels()
                    width = 1.pixels()
                    height = 100.percent() - 4.pixels()
                }.setColor(theme.divider) childOf container
            }
        }
    }

    private fun createFilterTypeButtons(parent: UIComponent, index: Int, filter: TrashHighlighter.FilteredItem) {
        val container = createBlock(2f).constrain {
            x = 67.percent()
            y = CenterConstraint()
            width = 28.percent()
            height = 20.pixels()
        }.setColor(theme.buttonGroup) childOf parent

        val options = listOf(
            FilterType.CONTAINS to "Contains",
            FilterType.EQUALS to "Equals",
            FilterType.REGEX to "Regex"
        )

        options.forEachIndexed { optionIndex, (type, name) ->
            createButtonGroupOption(
                name,
                filter.selectedFilter == type,
                optionIndex == 0,
                optionIndex == options.size - 1
            ) {
                updateFilter(index, filter.textInput, type, filter.selectedInput)
            }.constrain {
                x = (optionIndex * 33.33).percent()
                y = 0.percent()
                width = 33.33.percent()
                height = 100.percent()
            } childOf container

            if (optionIndex < options.size - 1) {
                createBlock(0f).constrain {
                    x = ((optionIndex + 1) * 33.33).percent()
                    y = 2.pixels()
                    width = 1.pixels()
                    height = 100.percent() - 4.pixels()
                }.setColor(theme.divider) childOf container
            }
        }
    }

    private fun createButtonGroupOption(
        text: String,
        selected: Boolean,
        isFirst: Boolean,
        isLast: Boolean,
        onClick: () -> Unit
    ): UIComponent {
        val radius = when {
            isFirst -> 2f
            isLast -> 2f
            else -> 0f
        }

        val buttonBorder = createBlock(radius).setColor(theme.buttonSelected)

        val button = createBlock(radius).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent() - 2.pixels()
            height = 100.percent() - 2.pixels()
            color = theme.buttonGroup.constraint
        } childOf buttonBorder

        buttonBorder.setColor(if (selected) theme.buttonSelected else Color(0,0,0,0))

        if (!selected) {
            button.onMouseEnter {
                buttonBorder.animate { setColorAnimation(Animations.OUT_EXP, 0.2f, theme.buttonHover.toConstraint()) }
            }.onMouseLeave {
                buttonBorder.animate { setColorAnimation(Animations.OUT_EXP, 0.2f, Color(0,0,0,0).toConstraint()) }
            }
        }

        button.onMouseClick {
            if (!selected) onClick()
        }

        UIText(text).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(if (selected) theme.accent else Color.WHITE) childOf button

        return buttonBorder
    }

    private fun createDeleteButton(parent: UIComponent, index: Int) {
        val deleteButton = createBlock(3f).constrain {
            x = 100.percent() - 28.pixels()
            y = CenterConstraint()
            width = 20.pixels()
            height = 20.pixels()
        }.setColor(theme.element) childOf parent

        deleteButton.onMouseEnter {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.danger.toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_EXP, 0.3f, theme.element.toConstraint()) }
        }.onMouseClick { removeFilter(index) }

        UIText("✕").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(Color.RED.darker()) childOf deleteButton
    }

    private fun addFilter() {
        val pattern = inputField.text.trim()
        if (pattern.isEmpty()) {
            ChatUtils.addMessage("§cEnter a pattern!")
            return
        }

        val currentFilters = TrashHighlighter.getFilters().toMutableList()
        currentFilters.add(TrashHighlighter.FilteredItem(pattern, FilterType.CONTAINS, InputType.ITEM_ID))
        TrashHighlighter.setFilters(currentFilters)
        inputField.text = ""
        renderFilters()
    }

    private fun updateFilter(index: Int, text: String, filterType: FilterType, inputType: InputType) {
        val currentFilters = TrashHighlighter.getFilters().toMutableList()
        if (index < currentFilters.size) {
            currentFilters[index] = TrashHighlighter.FilteredItem(text, filterType, inputType)
            TrashHighlighter.setFilters(currentFilters)
            renderFilters()
        }
    }

    private fun removeFilter(index: Int) {
        val currentFilters = TrashHighlighter.getFilters().toMutableList()
        if (index < currentFilters.size) {
            currentFilters.removeAt(index)
            TrashHighlighter.setFilters(currentFilters)
            renderFilters()
        }
    }

    private fun Color.withAlpha(alpha: Int): Color = Color(red, green, blue, alpha)
}