package meowing.zen.config.ui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UKeyboard
import meowing.zen.config.ui.constraint.ChildHeightConstraint
import meowing.zen.config.ui.core.ConfigTheme
import meowing.zen.config.ui.core.ConfigValidator
import meowing.zen.config.ui.core.ElementFactory
import meowing.zen.config.ui.elements.ColorPicker
import meowing.zen.config.ui.elements.Dropdown
import meowing.zen.config.ui.types.*
import meowing.zen.utils.DataUtils
import meowing.zen.utils.Utils.createBlock
import meowing.zen.utils.Utils.toColorFromMap
import java.awt.Color

typealias ConfigData = Map<String, Any>

class ConfigUI(configFileName: String = "config") : WindowScreen(ElementaVersion.V10, true, false, true, 2) {
    private val dataUtils = DataUtils(configFileName, mutableMapOf<String, Any>())
    private val config: MutableMap<String, Any> = dataUtils.getData()
    private val validator = ConfigValidator()
    private val theme = ConfigTheme()
    private val factory = ElementFactory(theme)

    private val categories = mutableListOf<ConfigCategory>()
    private val sections = mutableMapOf<String, MutableList<ConfigSection>>()
    private val subcategories = mutableMapOf<String, MutableList<ConfigSubcategory>>()
    private val elementContainers = mutableMapOf<String, UIComponent>()
    private val elementRefs = mutableMapOf<String, ConfigElement>()
    private val configListeners = mutableMapOf<String, MutableList<(Any) -> Unit>>()
    private val sectionToggleElements = mutableMapOf<String, String>()
    private val sectionToggleRefs = mutableMapOf<String, UIComponent>()

    private var activeCategory: String? = null
    private var activeSection: String? = null

    private lateinit var categoryScroll: ScrollComponent
    private lateinit var sectionScroll: ScrollComponent
    private lateinit var elementScroll: ScrollComponent

    init {
        createGUI()
    }

    private fun createGUI() {
        val main = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 70.percent()
            height = 65.percent()
        }.effect(OutlineEffect(theme.accent2, 1f)) childOf window
        createPanels(main)
    }

    private fun createPanels(parent: UIComponent) {
        val categoryPanel = createBlock(2f).constrain {
            x = 0.percent()
            y = 0.percent()
            width = 15.percent()
            height = 100.percent()
        }.setColor(theme.bg) childOf parent

        createBlock(0f).constrain {
            x = 15.percent()
            y = 0.percent()
            width = 1.pixels()
            height = 100.percent()
        }.setColor(theme.accent2.darker()) childOf parent

        categoryScroll = ScrollComponent().constrain {
            x = 2.percent()
            y = 2.percent()
            width = 96.percent()
            height = 96.percent()
        } childOf categoryPanel

        val sectionPanel = createBlock(2f).constrain {
            x = 15.percent() + 1.pixels()
            y = 0.percent()
            width = 30.percent() - 1.pixels()
            height = 100.percent()
        }.setColor(theme.panel) childOf parent

        createBlock(0f).constrain {
            x = 44.9.percent()
            y = 0.percent()
            width = 1.pixels()
            height = 100.percent()
        }.setColor(theme.accent2.darker()) childOf parent

        sectionScroll = ScrollComponent().constrain {
            x = 2.percent()
            y = 2.percent()
            width = 96.percent()
            height = 96.percent()
        } childOf sectionPanel

        val elementPanel = createBlock(2f).constrain {
            x = 44.9.percent() + 1.pixels()
            y = 0.percent()
            width = 55.percent()
            height = 100.percent()
        }.setColor(theme.popup) childOf parent

        elementScroll = ScrollComponent().constrain {
            x = 2.percent()
            y = 2.percent()
            width = 96.percent()
            height = 96.percent()
        } childOf elementPanel
    }

    private fun createCategory(text: String, isActive: Boolean, onClick: () -> Unit): UIComponent {
        val item = createBlock(3f).constrain {
            x = (-100).percent()
            y = CramSiblingConstraint(2f)
            width = 95.percent()
            height = 24.pixels()
        }.setColor(if (isActive) theme.accent.withAlpha(60) else Color(0,0,0,0))

        if (!isActive) {
            item.onMouseEnter {
                animate { setColorAnimation(Animations.OUT_QUAD, 0.2f, theme.accent2.withAlpha(30).toConstraint()) }
            }.onMouseLeave {
                animate { setColorAnimation(Animations.OUT_QUAD, 0.2f, Color(0,0,0,0).toConstraint()) }
            }.onMouseClick { onClick() }
        }

        UIText(text).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.9.pixels()
        }.setColor(if (isActive) theme.accent else theme.accent2) childOf item

        return item
    }

    private fun createSectionWithToggle(section: ConfigSection, isActive: Boolean, onClick: () -> Unit): UIComponent {
        val sectionKey = "${activeCategory}-${section.name}"
        val toggleConfigKey = sectionToggleElements[sectionKey]
        val nonToggleElements = subcategories[sectionKey]?.flatMap { it.elements }?.filter { it.configKey != toggleConfigKey } ?: emptyList()
        val hasElements = nonToggleElements.isNotEmpty()

        val item = createBlock(3f).constrain {
            x = 0.percent()
            y = CramSiblingConstraint(2f)
            width = 100.percent()
            height = 24.pixels()
        }.setColor(if (isActive) theme.accent.withAlpha(60) else Color(0,0,0,0))

        if (!isActive) {
            item.onMouseEnter {
                animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, theme.accent2.withAlpha(30).toConstraint()) }
            }.onMouseLeave {
                animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, Color(0,0,0,0).toConstraint()) }
            }.onMouseClick { if (hasElements) onClick() }
        }

        UIText(section.name).constrain {
            x = 8.pixels()
            y = CenterConstraint()
            textScale = 0.9.pixels()
        }.setColor(theme.accent2) childOf item

        toggleConfigKey?.let { key ->
            val toggleElement = subcategories[sectionKey]?.flatMap { it.elements }?.find { it.configKey == key }
            if (toggleElement?.type is ElementType.Switch) {
                val currentValue = getConfigValue(key) as? Boolean ?: toggleElement.type.default
                val switchElement = ConfigElement(key, toggleElement.title ?: "", ElementType.Switch(currentValue))
                val toggleSwitch = factory.createSwitch(switchElement, config, 2f, 35f) { updateConfig(key, it) }

                if (hasElements) {
                    UIImage.ofResource("/assets/zen/logos/gear.png").constrain {
                        x = 28.pixels(true)
                        y = CenterConstraint()
                        width = 14.pixels()
                        height = 14.pixels()
                    } childOf item
                }

                toggleSwitch.constrain {
                    x = RelativeConstraint(1f) - 30.pixels()
                    y = CenterConstraint()
                    width = 20.pixels()
                    height = 10.pixels()
                }.onMouseClick { it.stopPropagation() } childOf item

                sectionToggleRefs[sectionKey] = toggleSwitch
            }
        }
        return item
    }

    private fun updateCategories() {
        categoryScroll.clearChildren()
        UIContainer().constrain {
            width = 100.percent()
            height = ChildHeightConstraint(2f)
        }.also { container ->
            categories.forEach { category ->
                createCategory(category.name, category.name == activeCategory) {
                    switchCategory(category.name)
                }.setX(CenterConstraint()) childOf container
            }
        } childOf categoryScroll
    }

    private fun updateSections() {
        sectionScroll.clearChildren()
        activeCategory?.let { categoryName ->
            sections[categoryName]?.let { sectionList ->
                UIContainer().constrain {
                    width = 100.percent()
                    height = ChildHeightConstraint(2f)
                }.also { container ->
                    sectionList.forEach { section ->
                        createSectionWithToggle(section, section.name == activeSection) {
                            switchSection(section.name)
                        } childOf container
                    }
                } childOf sectionScroll
            }
        }
    }

    private fun updateElements() {
        elementScroll.clearChildren()
        activeSection?.let { sectionName ->
            val sectionKey = "${activeCategory}-${sectionName}"
            val toggleConfigKey = sectionToggleElements[sectionKey]

            subcategories[sectionKey]?.let { subcatList ->
                UIContainer().constrain {
                    width = 100.percent()
                    height = ChildHeightConstraint(6f)
                }.also { container ->
                    subcatList.forEach { subcat ->
                        val nonToggleElements = subcat.elements.filter { it.configKey != toggleConfigKey }
                        if (nonToggleElements.isNotEmpty()) {
                            createSubcategoryHeader(container, subcat.name)
                            subcat.elements.forEach { element ->
                                if (element.configKey != toggleConfigKey) {
                                    createElementUI(container, element)
                                }
                            }
                        }
                    }
                } childOf elementScroll
            }
        }
    }

    private fun createSubcategoryHeader(parent: UIComponent, name: String) {
        val dividerContainer = UIContainer().constrain {
            x = 0.percent()
            y = CramSiblingConstraint(8f)
            width = 100.percent()
            height = 16.pixels()
        } childOf parent

        createBlock(0f).constrain {
            x = 0.percent()
            y = CenterConstraint()
            width = 35.percent()
            height = 1.pixels()
        }.setColor(theme.accent2) childOf dividerContainer

        UIText(name).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(theme.accent) childOf dividerContainer

        createBlock(0f).constrain {
            x = 65.percent()
            y = CenterConstraint()
            width = 35.percent()
            height = 1.pixels()
        }.setColor(theme.accent2) childOf dividerContainer
    }

    private fun createElementUI(parent: UIComponent, element: ConfigElement) {
        val isSlider = element.type is ElementType.Slider
        val elementHeight = if (isSlider) 48.pixels() else 28.pixels()

        val elementContainer = UIContainer().constrain {
            x = 0.percent()
            y = CramSiblingConstraint(6f)
            width = 100.percent()
            height = elementHeight
        } childOf parent

        val card = createBlock(3f).constrain {
            x = 2.percent()
            y = 0.percent()
            width = 96.percent()
            height = 100.percent()
        }.setColor(theme.accent) childOf elementContainer

        val innerCard = createBlock(3f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 99.7.percent()
            height = if (isSlider) 97.5.percent() else 96.5.percent()
        }.setColor(theme.bg) childOf card

        element.title?.let { title ->
            UIText(title).constrain {
                x = 8.pixels()
                y = if (isSlider) 6.pixels() else CenterConstraint()
                textScale = 0.8.pixels()
            }.setColor(theme.accent) childOf innerCard
        }

        val widget = createElementWidget(element)
        widget.constrain {
            x = if (isSlider) 8.pixels() else RelativeConstraint(1f) - 60.pixels()
            y = if (isSlider) 22.pixels() else CenterConstraint()
            width = if (isSlider) RelativeConstraint(1f) - 4.pixels() else 50.pixels()
            height = if (isSlider) 18.pixels() else 16.pixels()
        } childOf card

        elementContainers[element.configKey] = elementContainer
        elementRefs[element.configKey] = element
        updateElementVisibility(element.configKey)
    }

    private fun createElementWidget(element: ConfigElement): UIComponent {
        return when (element.type) {
            is ElementType.Button -> factory.createButton(element, config, this)
            is ElementType.Switch -> factory.createSwitch(element, config) { updateConfig(element.configKey, it) }
            is ElementType.Slider -> factory.createSlider(element, config) { updateConfig(element.configKey, it) }
            is ElementType.Dropdown -> factory.createDropdown(element, config) { updateConfig(element.configKey, it) }
            is ElementType.TextInput -> factory.createTextInput(element, config) { updateConfig(element.configKey, it) }
            is ElementType.TextParagraph -> factory.createTextParagraph(element)
            is ElementType.ColorPicker -> factory.createColorPicker(element, config) { updateConfig(element.configKey, it) }
            is ElementType.Keybind -> factory.createKeybind(element, config) { updateConfig(element.configKey, it) }
        }
    }

    private fun updateConfig(configKey: String, newValue: Any) {
        val validatedValue = validator.validate(configKey, newValue) ?: return

        val serializedValue = when (validatedValue) {
            is Color -> mapOf(
                "r" to validatedValue.red,
                "g" to validatedValue.green,
                "b" to validatedValue.blue,
                "a" to validatedValue.alpha
            )
            else -> validatedValue
        }

        config[configKey] = serializedValue
        dataUtils.setData(config)
        updateElementVisibilities()
        configListeners[configKey]?.forEach { it(validatedValue) }
        updateSectionToggles()
    }

    private fun updateSectionToggles() {
        sectionToggleRefs.forEach { (sectionKey, toggleRef) ->
            sectionToggleElements[sectionKey]?.let { toggleConfigKey ->
                val newValue = getConfigValue(toggleConfigKey) as? Boolean ?: false
                factory.updateSwitchValue(toggleRef, newValue)
            }
        }
    }

    private fun updateElementVisibilities() {
        elementRefs.keys.forEach { updateElementVisibility(it) }
    }

    private fun updateElementVisibility(configKey: String) {
        val container = elementContainers[configKey] ?: return
        val element = elementRefs[configKey] ?: return
        val visible = element.shouldShow(config)
        if (visible) container.unhide(true) else container.hide()
    }

    private fun switchCategory(categoryName: String) {
        if (activeCategory == categoryName) return
        activeCategory = categoryName
        activeSection = null
        updateCategories()
        updateSections()
        elementScroll.clearChildren()

        sections[categoryName]?.firstOrNull()?.let { firstSection ->
            activeSection = firstSection.name
            updateSections()
            updateElements()
        }
    }

    private fun switchSection(sectionName: String) {
        if (activeSection == sectionName) return
        activeSection = sectionName
        updateSections()
        updateElements()
    }

    private fun getDefaultValue(type: ElementType?): Any? = when (type) {
        is ElementType.Switch -> type.default
        is ElementType.Slider -> type.default
        is ElementType.Dropdown -> type.default
        is ElementType.TextInput -> type.default
        is ElementType.ColorPicker -> type.default
        is ElementType.Keybind -> type.default
        else -> null
    }

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        if (keyCode == 1) {
            if (ColorPicker.isPickerOpen) {
                ColorPicker.closePicker()
                return
            }
            if (Dropdown.isDropdownOpen) {
                Dropdown.closeDropdown()
                return
            }
        }
        super.onKeyPressed(keyCode, typedChar, modifiers)
    }

    override fun onScreenClose() {
        super.onScreenClose()
        saveConfig()
    }

    fun addElement(categoryName: String, sectionName: String, element: ConfigElement, isSectionToggle: Boolean = false) =
        addElement(categoryName, sectionName, "Backwards Config", element, isSectionToggle)

    fun addElement(categoryName: String, sectionName: String, subcategoryName: String, element: ConfigElement, isSectionToggle: Boolean = false): ConfigUI {
        val isFirstCategory = categories.isEmpty()

        categories.find { it.name == categoryName } ?: ConfigCategory(categoryName).also { categories.add(it) }

        val sectionList = sections.getOrPut(categoryName) { mutableListOf() }
        sectionList.find { it.name == sectionName } ?: ConfigSection(sectionName).also { sectionList.add(it) }

        val sectionKey = "${categoryName}-${sectionName}"
        val subcategoryList = subcategories.getOrPut(sectionKey) { mutableListOf() }
        val subcategory = subcategoryList.find { it.name == subcategoryName } ?: ConfigSubcategory(subcategoryName).also { subcategoryList.add(it) }

        subcategory.elements.add(element)

        if (isSectionToggle && element.type is ElementType.Switch) {
            sectionToggleElements[sectionKey] = element.configKey
        }

        getDefaultValue(element.type)?.let { defaultValue ->
            if (!config.containsKey(element.configKey)) {
                config[element.configKey] = defaultValue
                dataUtils.setData(config)
                configListeners[element.configKey]?.forEach { it(defaultValue) }
            }
        }

        registerValidator(element)

        if (isFirstCategory) {
            activeCategory = categoryName
            updateCategories()
            sections[categoryName]?.firstOrNull()?.let {
                activeSection = it.name
                updateSections()
                updateElements()
            }
        } else {
            updateCategories()
            if (activeCategory == categoryName) {
                updateSections()
                if (activeSection == sectionName) updateElements()
            }
        }
        return this
    }

    private fun registerValidator(element: ConfigElement) {
        val configValue = when (val type = element.type) {
            is ElementType.Switch -> ConfigValue.BooleanValue(type.default)
            is ElementType.Slider -> ConfigValue.DoubleValue(type.default, type.min, type.max)
            is ElementType.Dropdown -> ConfigValue.IntValue(type.default, 0, type.options.size - 1)
            is ElementType.TextInput -> ConfigValue.StringValue(type.default, type.maxLength)
            is ElementType.ColorPicker -> ConfigValue.ColorValue(type.default)
            is ElementType.Keybind -> ConfigValue.IntValue(type.default)
            else -> null
        }
        configValue?.let { validator.register(element.configKey, it) }
    }

    fun registerListener(configKey: String, listener: (Any) -> Unit): ConfigUI {
        configListeners.getOrPut(configKey) { mutableListOf() }.add(listener)
        (getConfigValue(configKey) ?: getDefaultValue(elementRefs[configKey]?.type))?.let { currentValue ->
            val resolvedValue = when (currentValue) {
                is Map<*, *> -> currentValue.toColorFromMap()
                else -> currentValue
            }
            resolvedValue?.let { listener(it) }
        }
        return this
    }

    fun getConfigValue(configKey: String): Any? = config[configKey]
    fun getDefaultValue(configKey: String): Any? = elementRefs[configKey]?.type?.let { getDefaultValue(it) }
    fun saveConfig() = dataUtils.save()

    private fun Color.withAlpha(alpha: Int): Color = Color(red, green, blue, alpha)
}