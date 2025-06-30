package meowing.zen.config.ui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.universal.UKeyboard
import meowing.zen.config.ui.constraint.ChildHeightConstraint
import meowing.zen.config.ui.types.*
import meowing.zen.config.ui.core.*
import meowing.zen.utils.DataUtils

typealias ConfigData = Map<String, Any>

class ConfigUI(configFileName: String = "config") : WindowScreen(ElementaVersion.V10, true, false, true, 2) {
    private val dataUtils = DataUtils(configFileName, mutableMapOf<String, Any>())
    private val config: MutableMap<String, Any> = dataUtils.getData()
    private val validator = ConfigValidator()
    private val theme = ConfigTheme()
    private val factory = ElementFactory(theme)
    private val uiBuilder = UIBuilder(theme)

    private val categories = mutableListOf<ConfigCategory>()
    private var activeCategory: String? = null
    private val elementContainers = mutableMapOf<String, UIComponent>()
    private val elementRefs = mutableMapOf<String, ConfigElement>()
    private val configListeners = mutableMapOf<String, MutableList<(Any) -> Unit>>()

    private lateinit var leftPanel: UIRoundedRectangle
    private lateinit var rightPanel: UIRoundedRectangle
    private lateinit var categoryScroll: ScrollComponent
    private lateinit var elementScroll: ScrollComponent
    private var contentContainer: UIContainer? = null

    private val visibilityCache = mutableMapOf<String, Boolean>()
    private var needsVisibilityUpdate = false

    companion object {
        var activePopup: UIComponent? = null
    }

    init {
        createGUI()
    }

    private fun createGUI() {
        val main = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 85.percent()
            height = 80.percent()
        } childOf window

        initializePanels(main)
    }

    private fun initializePanels(parent: UIComponent) {
        leftPanel = (UIRoundedRectangle(2f).constrain {
            x = 0.percent()
            y = 0.percent()
            width = 18.percent()
            height = 100.percent()
        }.setColor(theme.bg) childOf parent) as UIRoundedRectangle

        UIText("Zen").constrain {
            x = CenterConstraint()
            y = 15.pixels()
            textScale = 1.4.pixels()
        }.setColor(theme.accent) childOf leftPanel

        categoryScroll = ScrollComponent().constrain {
            x = CenterConstraint()
            y = 40.pixels()
            width = 92.percent()
            height = RelativeConstraint(1f) - 36.pixels()
        } childOf leftPanel

        uiBuilder.createHudButton() childOf leftPanel

        rightPanel = (UIRoundedRectangle(2f).constrain {
            x = 18.percent()
            y = 0.percent()
            width = 82.percent()
            height = 100.percent()
        }.setColor(theme.panel) childOf parent) as UIRoundedRectangle

        elementScroll = ScrollComponent().constrain {
            x = 3.percent()
            y = 3.percent()
            width = 94.percent()
            height = 94.percent()
        } childOf rightPanel
    }

    private fun updateCategories() {
        categoryScroll.clearChildren()
        val container = UIContainer().constrain {
            width = 100.percent()
            height = ChildBasedSizeConstraint(3f)
        } childOf categoryScroll

        categories.forEach { category ->
            val isActive = category.name == activeCategory
            uiBuilder.createCategoryButton(category, isActive) {
                switchCategory(category.name)
            } childOf container
        }
    }

    private fun updateSections(categoryName: String) {
        elementScroll.clearChildren()
        val category = categories.find { it.name == categoryName } ?: return

        contentContainer = UIContainer().constrain {
            width = 100.percent()
            height = ChildHeightConstraint(8f)
        } childOf elementScroll

        category.sections.forEach { section ->
            uiBuilder.createSectionCard(section) {
                createPopup(section)
            } childOf contentContainer!!
        }
    }

    private fun createPopup(section: ConfigSection) {
        val overlay = uiBuilder.createPopupOverlay { closePopup() } childOf rightPanel
        val popup = uiBuilder.createPopup() childOf overlay

        uiBuilder.createPopupHeader(section.name) childOf popup
        createPopupContent(popup, section)
        uiBuilder.createCloseButton { closePopup() } childOf popup

        activePopup = overlay
    }

    private fun createPopupContent(parent: UIComponent, section: ConfigSection) {
        val scroll = ScrollComponent().constrain {
            x = 3.percent()
            y = 18.percent()
            width = 94.percent()
            height = 78.percent()
        } childOf parent

        val container = UIContainer().constrain {
            width = 100.percent()
            height = ChildBasedSizeConstraint(8f)
        } childOf scroll

        section.elements.forEachIndexed { index, element ->
            createElementUI(container, element, index == 0)
        }
    }

    private fun createElementUI(parent: UIComponent, element: ConfigElement, isFirst: Boolean) {
        val outerContainer = UIContainer().constrain {
            x = 0.percent()
            y = if (isFirst) 5.pixels() else CramSiblingConstraint(15f)
            width = 100.percent()
            height = 55.pixels()
        } childOf parent

        val card = createElementCard(outerContainer)
        createElementWidget(card, element)

        if (element.title != null) createElementTitle(outerContainer, element.title)
        if (element.description != null && !isDescriptionWidget(element.type)) createElementDescription(card, element.description)

        elementContainers[element.configKey] = outerContainer
        elementRefs[element.configKey] = element
        updateElementVisibility(element.configKey)
    }

    private fun createElementCard(parent: UIComponent): UIComponent {
        val card = UIRoundedRectangle(5f).constrain {
            x = 0.percent()
            y = 10.pixels()
            width = 100.percent()
            height = 45.pixels()
        }.setColor(theme.accent) childOf parent

        UIRoundedRectangle(5f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 99.8.percent()
            height = 97.5.percent()
        }.setColor(theme.popup) childOf card

        return card
    }

    private fun createElementTitle(parent: UIComponent, title: String) {
        UIText(title).constrain {
            x = 3.pixels()
            y = 0.pixels()
            textScale = 0.9.pixels()
        }.setColor(theme.accent) childOf parent
    }

    private fun createElementDescription(parent: UIComponent, description: String) {
        UIWrappedText(description).constrain {
            x = 18.percent()
            y = CenterConstraint()
            width = 78.percent()
            textScale = 0.8.pixels()
        }.setColor(theme.accent) childOf parent
    }

    private fun createElementWidget(parent: UIComponent, element: ConfigElement) {
        val widget = when (element.type) {
            is ElementType.Button -> factory.createButton(element, config, this)
            is ElementType.Switch -> factory.createSwitch(element, config) { updateConfig(element.configKey, it) }
            is ElementType.Slider -> factory.createSlider(element, config) { updateConfig(element.configKey, it) }
            is ElementType.Dropdown -> factory.createDropdown(element, config) { updateConfig(element.configKey, it) }
            is ElementType.TextInput -> factory.createTextInput(element, config) { updateConfig(element.configKey, it) }
            is ElementType.TextParagraph -> factory.createTextParagraph(element)
            is ElementType.ColorPicker -> factory.createColorPicker(element, config) { updateConfig(element.configKey, it) }
        }

        val constraints = when (element.type) {
            is ElementType.ColorPicker -> {
                widget.constrain {
                    x = 2.5.percent()
                    y = CenterConstraint()
                    width = 96.percent()
                    height = 42.pixels()
                }
            }
            is ElementType.TextParagraph -> {
                widget.constrain {
                    x = CenterConstraint()
                    y = CenterConstraint()
                    width = 96.percent()
                    height = 22.pixels()
                }
            }
            else -> {
                widget.constrain {
                    x = 2.5.percent()
                    y = CenterConstraint()
                    width = 75.pixels()
                    height = if (element.type is ElementType.Slider) 14.pixels() else 24.pixels()
                }
            }
        }
        constraints childOf parent
    }

    private fun isDescriptionWidget(type: ElementType): Boolean {
        return type is ElementType.ColorPicker || type is ElementType.TextParagraph
    }

    private fun updateConfig(configKey: String, newValue: Any) {
        val validatedValue = validator.validate(configKey, newValue) ?: return
        config[configKey] = validatedValue
        dataUtils.setData(config)

        needsVisibilityUpdate = true
        scheduleVisibilityUpdate()

        configListeners[configKey]?.forEach { it(validatedValue) }
    }

    private fun scheduleVisibilityUpdate() {
        if (!needsVisibilityUpdate) return

        elementContainers.keys.forEach { key ->
            val element = elementRefs[key] ?: return@forEach
            val visible = element.shouldShow(config)
            val cachedVisible = visibilityCache[key]

            if (cachedVisible != visible) {
                visibilityCache[key] = visible
                updateElementVisibility(key)
            }
        }

        needsVisibilityUpdate = false
    }

    private fun updateElementVisibility(configKey: String) {
        val container = elementContainers[configKey] ?: return
        val element = elementRefs[configKey] ?: return
        val visible = element.shouldShow(config)

        container.constrain {
            height = if (visible) 55.pixels() else 0.pixels()
        }

        if (visible) container.unhide(true) else container.hide(true)
    }

    private fun closePopup() {
        activePopup?.let { popup ->
            popup.parent.removeChild(popup)
            activePopup = null
        }
    }

    private fun switchCategory(categoryName: String) {
        if (activeCategory == categoryName) return
        closePopup()
        activeCategory = categoryName
        updateCategories()
        updateSections(categoryName)
    }

    private fun getDefaultValue(type: ElementType?): Any? = when (type) {
        is ElementType.Switch -> type.default
        is ElementType.Slider -> type.default
        is ElementType.Dropdown -> type.default
        is ElementType.TextInput -> type.default
        is ElementType.ColorPicker -> type.default
        else -> null
    }

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        if (keyCode == 1 && activePopup != null) {
            closePopup()
            return
        }
        super.onKeyPressed(keyCode, typedChar, modifiers)
    }

    override fun onScreenClose() {
        super.onScreenClose()
        saveConfig()
    }

    fun addElement(categoryName: String, sectionName: String, element: ConfigElement): ConfigUI {
        val isFirstCat = categories.isEmpty()
        val category = categories.find { it.name == categoryName }
            ?: ConfigCategory(categoryName).also {
                categories.add(it)
                updateCategories()
            }

        val section = category.sections.find { it.name == sectionName }
            ?: ConfigSection(sectionName).also {
                category.sections.add(it)
            }

        section.elements.add(element)

        val defaultValue = getDefaultValue(element.type)
        if (defaultValue != null && !config.containsKey(element.configKey)) {
            config[element.configKey] = defaultValue
            dataUtils.setData(config)
        }

        registerValidator(element)

        if (isFirstCat) switchCategory(categoryName)
        else if (activeCategory == categoryName) updateSections(categoryName)
        return this
    }

    private fun registerValidator(element: ConfigElement) {
        val configValue = when (val type = element.type) {
            is ElementType.Switch -> ConfigValue.BooleanValue(type.default)
            is ElementType.Slider -> ConfigValue.DoubleValue(type.default, type.min, type.max)
            is ElementType.Dropdown -> ConfigValue.IntValue(type.default, 0, type.options.size - 1)
            is ElementType.TextInput -> ConfigValue.StringValue(type.default, type.maxLength)
            is ElementType.ColorPicker -> ConfigValue.ColorValue(type.default)
            else -> null
        }

        configValue?.let { validator.register(element.configKey, it) }
    }

    fun registerListener(configKey: String, listener: (Any) -> Unit): ConfigUI {
        configListeners.getOrPut(configKey) { mutableListOf() }.add(listener)
        val currentValue = config[configKey] ?: getDefaultValue(elementRefs[configKey]?.type)
        currentValue?.let { listener(it) }
        return this
    }

    fun getConfigValue(configKey: String): Any? = config[configKey]

    fun saveConfig() = dataUtils.save()
}