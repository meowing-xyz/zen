package meowing.zen.features

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import meowing.zen.UpdateGUI
import meowing.zen.Zen
import meowing.zen.Zen.Companion.features
import meowing.zen.Zen.Companion.mc
import meowing.zen.Zen.Companion.prefix
import meowing.zen.api.ItemApi
import meowing.zen.api.PetTracker
import meowing.zen.api.PlayerStats
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.constraint.ChildHeightConstraint
import meowing.zen.config.ui.core.CustomFontProvider
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EventBus
import meowing.zen.events.ItemTooltipEvent
import meowing.zen.utils.*
import meowing.zen.utils.ItemUtils.extraAttributes
import meowing.zen.utils.ItemUtils.skyblockID
import meowing.zen.utils.LoopUtils.setTimeout
import net.minecraft.command.ICommandSender
import net.minecraft.nbt.NBTTagByteArray
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.text.DecimalFormat

@Zen.Module
object Debug : Feature() {
    data class PersistentData(var debugmode: Boolean = false)
    val data = DataUtils("Debug", PersistentData())

    private var copyingTooltip = false

    init {
        createCustomEvent<ItemTooltipEvent>("tooltip") { event ->
            if (event.itemStack.extraAttributes != null && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    event.lines.clear()
                    val id = event.itemStack.skyblockID
                    val itemData = ItemApi.getItemInfo(id) ?: return@createCustomEvent
                    itemData.remove("nbttag")
                    itemData.remove("lore")

                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val lines = gson.toJson(itemData).replace("\"", "").split("\n")
                    event.lines.add("Generic Item Data")
                    event.lines.addAll(lines)
                } else {
                    val nbt = convertNBTtoJSON(event.itemStack.extraAttributes!!)
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val lines = gson.toJson(nbt).replace("\"", "").split("\n")
                    event.lines.addAll(lines)
                }
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
                val nbt = convertNBTtoJSON(event.itemStack.serializeNBT())
                val gson = GsonBuilder().setPrettyPrinting().create()
                val lines = gson.toJson(nbt).replace("\"", "").split("\n")
                event.lines.clear()
                event.lines.addAll(lines)
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_C) && !copyingTooltip) {
                copyingTooltip = true
                setTimeout(500) { copyingTooltip = false }
                val stringSelection = StringSelection(event.lines.joinToString("\n"))
                Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, null)
            }
        }

        if (debugmode) registerEvent("tooltip") else unregisterEvent("tooltip")
    }

    private fun convertNBTtoJSON(nbt: NBTTagCompound): JsonObject {
        val jsonObject = JsonObject()
        for (key in nbt.keySet) {
            val jsonElement = when (val tag = nbt.getTag(key)) {
                is NBTTagCompound -> convertNBTtoJSON(tag)
                is NBTTagList -> {
                    val jsonArray = JsonArray()
                    for (i in 0 until tag.tagCount()) {
                        when (val item = tag.get(i)) {
                            is NBTTagString -> jsonArray.add(JsonPrimitive(item.string))
                            is NBTTagCompound -> jsonArray.add(convertNBTtoJSON(item))
                            else -> jsonArray.add(JsonNull.INSTANCE)
                        }
                    }
                    jsonArray
                }
                is NBTTagByteArray -> JsonObject()
                else -> {
                    try {
                        JsonParser().parse(tag.toString())
                    } catch (e: Exception) {
                        JsonNull.INSTANCE
                    }
                }
            }
            jsonObject.add(key, jsonElement)
        }
        return jsonObject
    }

    inline val debugmode get() = data.getData().debugmode

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        if (!debugmode) {
            return configUI
        }

        return configUI
            .addElement("Debug", "Config Test", "Debug GUI", ConfigElement(
                "debuggui",
                "Debug Information GUI",
                ElementType.Button("Open Debug GUI") {
                    TickUtils.schedule(2) {
                        mc.displayGuiScreen(DebugGui())
                    }
                }
            ))
            .addElement("Debug", "Config Test", "Switch", ConfigElement(
                "test_switch",
                "Switch test",
                ElementType.Switch(false)
            ))
            .addElement("Debug", "Config Test", "Button", ConfigElement(
                "test_button",
                "Button test",
                ElementType.Button("Click Me!") {
                    LOGGER.info("Button clicked!")
                }
            ))
            .addElement("Debug", "Config Test", "Slider", ConfigElement(
                "test_slider",
                "Slider test",
                ElementType.Slider(0.0, 100.0, 50.0, false)
            ))
            .addElement("Debug", "Config Test", "Slider Double", ConfigElement(
                "test_slider_double",
                "Slider double test",
                ElementType.Slider(0.0, 10.0, 5.5, true)
            ))
            .addElement("Debug", "Config Test", "Dropdown", ConfigElement(
                "test_dropdown",
                "Dropdown test",
                ElementType.Dropdown(listOf("Option 1", "Option 2", "Option 3", "Option 4"), 0)
            ))
            .addElement("Debug", "Config Test", "Text Input", ConfigElement(
                "test_textinput",
                "Text input test",
                ElementType.TextInput("Default text", "Enter text here...", 50)
            ))
            .addElement("Debug", "Config Test", "Text Input Empty", ConfigElement(
                "test_textinput_empty",
                "Empty text input test",
                ElementType.TextInput("", "Type something...", 100)
            ))
            .addElement("Debug", "Config Test", "Text Paragraph", ConfigElement(
                "test_paragraph",
                null,
                ElementType.TextParagraph("This is a text paragraph element used for displaying information or instructions to the user. It can contain multiple lines of text.")
            ))
            .addElement("Debug", "Config Test", "Color Picker", ConfigElement(
                "test_colorpicker",
                "Color picker test",
                ElementType.ColorPicker(Color(100, 200, 255))
            ))
            .addElement("Debug", "Config Test", "Keybind", ConfigElement(
                "test_keybind",
                "Keybind test",
                ElementType.Keybind(82)
            ))
            .addElement("Debug", "Config Test", "Multi Checkbox", ConfigElement(
                "test_multicheckbox",
                "Multi checkbox test",
                ElementType.MultiCheckbox(
                    options = listOf("Feature A", "Feature B", "Feature C", "Feature D", "Feature E"),
                    default = setOf(0, 2)
                )
            ))
    }
}



@Zen.Command
object DebugCommand : CommandUtils("zendebug", aliases = listOf("zd")) {
    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        val stringArgs = args?.filterNotNull()?.toTypedArray() ?: return
        when (stringArgs.getOrNull(0)?.lowercase()) {
            "toggle" -> {
                Debug.data.getData().debugmode = !Debug.data.getData().debugmode
                Debug.data.save()
                if (Debug.debugmode) Debug.registerEvent("tooltip") else Debug.unregisterEvent("tooltip")
                ChatUtils.addMessage("$prefix §fToggled dev mode (${Debug.debugmode}). You will need to restart to see the difference in the Config UI")
            }
            "stats" -> {
                ChatUtils.addMessage(
                    "§cHealth: ${PlayerStats.health} | Max: ${PlayerStats.maxHealth} | §6Absorb: ${PlayerStats.absorption}\n" +
                            "§9Mana: ${PlayerStats.mana} | Max: ${PlayerStats.maxMana} | §3Overflow: ${PlayerStats.overflowMana}\n" +
                            "§dRift Time: ${PlayerStats.riftTimeSeconds} | Max: ${PlayerStats.maxRiftTime}\n" +
                            "§aDefense: ${PlayerStats.defense} | Effective: ${PlayerStats.effectiveHealth} | Effective Max: ${PlayerStats.maxEffectiveHealth}\n" +
                            "§fPet: ${PetTracker.name} | ${PetTracker.level} | ${PetTracker.item}"
                )
            }
            "dgutils" -> {
                ChatUtils.addMessage(
                    "Crypt Count: ${DungeonUtils.getCryptCount()}\n" +
                            "Current Class: ${DungeonUtils.getCurrentClass()} ${DungeonUtils.getCurrentLevel()}\n" +
                            "isMage: ${DungeonUtils.isMage()}\n" +
                            "Cata: ${DungeonUtils.getCurrentCata()}"
                )
            }
            "updatechecker" -> {
                TickUtils.schedule(2) {
                    mc.displayGuiScreen(UpdateGUI())
                }
            }
            "regfeats" -> {
                ChatUtils.addMessage("Features registered:")
                features.forEach {
                    if (it.isEnabled()) ChatUtils.addMessage("§f> §c${it.configKey}")
                }
            }
            else -> {
                ChatUtils.addMessage("$prefix §fUsage: §7/§bzendebug §c<toggle|stats|dgutils|info>")
                TickUtils.schedule(2) {
                    mc.displayGuiScreen(DebugGui())
                }
            }
        }
    }
}

class DebugGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    private val theme = object {
        val bg = Color(8, 12, 16, 255)
        val element = Color(18, 24, 28, 255)
        val elementHover = Color(25, 35, 40, 255)
        val accent = Color(170, 230, 240, 255)
        val accent2 = Color(120, 180, 200, 255)
        val success = Color(46, 125, 50, 255)
        val warning = Color(200, 140, 60, 255)
        val danger = Color(160, 80, 80, 255)
        val divider = Color(30, 35, 40, 255)
        val buttonHover = Color(60, 140, 160, 255)
        val buttonDanger = Color(140, 70, 70, 255)
    }

    private lateinit var scrollComponent: ScrollComponent
    private lateinit var debugContainer: UIContainer
    private val formatter = DecimalFormat("#,###.##")
    private var updateTicker = 0
    private val expandedEvents = mutableSetOf<String>()

    init {
        buildGui()
        updateDebugInfo()
        startAutoUpdate()
    }

    private fun createBlock(radius: Float): UIRoundedRectangle = UIRoundedRectangle(radius)

    private fun buildGui() {
        val border = createBlock(4f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 85.percent()
            height = 90.percent()
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
    }

    private fun createHeader(parent: UIComponent) {
        val header = UIContainer().constrain {
            x = 0.percent()
            y = 0.percent()
            width = 100.percent()
            height = 50.pixels()
        } childOf parent

        UIText("§lZen Debug Information").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 2.0.pixels()
        }.setColor(theme.accent).setFontProvider(CustomFontProvider) childOf header

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
            y = 58.pixels()
            width = 100.percent() - 16.pixels()
            height = 100.percent() - 106.pixels()
        } childOf parent

        scrollComponent = ScrollComponent().constrain {
            x = 4.pixels()
            y = 4.pixels()
            width = 100.percent() - 8.pixels()
            height = 100.percent() - 8.pixels()
        } childOf contentPanel

        debugContainer = UIContainer().constrain {
            width = 100.percent()
            height = ChildHeightConstraint(8f)
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

        val refreshButton = createBlock(3f).constrain {
            x = CenterConstraint() - 45.pixels()
            y = CenterConstraint()
            width = 80.pixels()
            height = 24.pixels()
        }.setColor(theme.element) childOf footer

        refreshButton.onMouseEnter {
            animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, theme.buttonHover.toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, theme.element.toConstraint()) }
        }.onMouseClick {
            updateDebugInfo()
        }

        UIText("Refresh").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(Color.WHITE).setFontProvider(CustomFontProvider) childOf refreshButton

        val closeButton = createBlock(3f).constrain {
            x = CenterConstraint() + 45.pixels()
            y = CenterConstraint()
            width = 80.pixels()
            height = 24.pixels()
        }.setColor(theme.element) childOf footer

        closeButton.onMouseEnter {
            animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, theme.buttonDanger.toConstraint()) }
        }.onMouseLeave {
            animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, theme.element.toConstraint()) }
        }.onMouseClick {
            mc.displayGuiScreen(null)
        }

        UIText("Close").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 0.8.pixels()
        }.setColor(Color.WHITE).setFontProvider(CustomFontProvider) childOf closeButton
    }

    private fun startAutoUpdate() {
        TickUtils.schedule(20) {
            updateTicker++
            if (updateTicker >= 3) {
                updateDebugInfo()
                updateTicker = 0
            }
        }
    }

    private fun updateDebugInfo() {
        debugContainer.clearChildren()

        createPlayerStats()
        createFeatureInfo()
        createDungeonInfo()
        createPetInfo()
        createEventListenersInfo()
    }

    private fun createPlayerStats() {
        createSectionHeader("Player Statistics")

        val statsPanel = createStatsPanel()

        createStatRow("Health", "${formatter.format(PlayerStats.health)} / ${formatter.format(PlayerStats.maxHealth)}", theme.accent, statsPanel)
        createStatRow("Absorption", formatter.format(PlayerStats.absorption), theme.accent, statsPanel)
        createStatRow("Mana", "${formatter.format(PlayerStats.mana)} / ${formatter.format(PlayerStats.maxMana)}", theme.accent, statsPanel)
        createStatRow("Overflow Mana", formatter.format(PlayerStats.overflowMana), theme.accent, statsPanel)
        createStatRow("Defense", formatter.format(PlayerStats.defense), theme.accent, statsPanel)
        createStatRow("Effective Health", formatter.format(PlayerStats.effectiveHealth), theme.accent, statsPanel)
        createStatRow("Max Effective Health", formatter.format(PlayerStats.maxEffectiveHealth), theme.accent, statsPanel)
        createStatRow("Rift Time", "${PlayerStats.riftTimeSeconds}s / ${PlayerStats.maxRiftTime}s", theme.accent, statsPanel)
    }

    private fun createFeatureInfo() {
        createSectionHeader("Feature Status")

        val featurePanel = createStatsPanel()
        val enabledFeatures = features.filter { it.isEnabled() }
        val totalFeatures = features.size

        createStatRow("Total Features", totalFeatures.toString(), theme.accent, featurePanel)
        createStatRow("Enabled Features", enabledFeatures.size.toString(), theme.success, featurePanel)
        createStatRow(
            "Disabled Features",
            (totalFeatures - enabledFeatures.size).toString(),
            theme.danger,
            featurePanel
        )

        createSectionHeader("Features")
        val activePanel = createStatsPanel()

        features.sortedBy { it.configKey ?: it.javaClass.name }.chunked(4).forEach { row ->
            val rowContainer = UIContainer().constrain {
                x = 0.percent()
                y = CramSiblingConstraint(2f)
                width = 100.percent()
                height = 24.pixels()
            } childOf activePanel

            row.forEachIndexed { index, feature ->
                val rowWidth = when {
                    row.size == 1 && index == 0 -> 100.percent()
                    row.size == 2 -> 50.percent()
                    row.size == 3 -> 33.33.percent()
                    else -> 25.percent()
                }
                val xPos = when (index) {
                    0 -> 0.percent()
                    1 -> if (row.size == 2) 50.percent() else if (row.size == 3) 33.33.percent() else 25.percent()
                    2 -> if (row.size == 3) 66.66.percent() else 50.percent()
                    3 -> 75.percent()
                    else -> 0.percent()
                }

                val cell = createBlock(2f).constrain {
                    x = xPos
                    y = 0.percent()
                    width = rowWidth - if (index < row.size - 1) 1.pixels() else 0.pixels()
                    height = 100.percent()
                }.setColor(theme.element) childOf rowContainer

                UIText(feature.configKey ?: feature.javaClass.name).constrain {
                    x = 8.pixels()
                    y = CenterConstraint()
                    textScale = 0.9.pixels()
                }.setColor(theme.accent).setFontProvider(CustomFontProvider) childOf cell

                UIText(if (feature.isEnabled()) "Enabled" else "Disabled").constrain {
                    x = 8.pixels(alignOpposite = true)
                    y = CenterConstraint()
                    textScale = 0.9.pixels()
                }.setColor(if (feature.isEnabled()) theme.success else theme.danger).setFontProvider(CustomFontProvider) childOf cell
            }
        }
    }

    private fun createDungeonInfo() {
        createSectionHeader("Dungeon Information")

        val dungeonPanel = createStatsPanel()

        createStatRow("Current Class", DungeonUtils.getCurrentClass() ?: "None", theme.accent, dungeonPanel)
        createStatRow("Class Level", DungeonUtils.getCurrentLevel().toString(), theme.accent2, dungeonPanel)
        createStatRow("Catacombs Level", DungeonUtils.getCurrentCata().toString(), theme.accent2, dungeonPanel)
        createStatRow("Crypt Count", DungeonUtils.getCryptCount().toString(), theme.warning, dungeonPanel)
        createStatRow(
            "Is Mage", if (DungeonUtils.isMage()) "Yes" else "No",
            if (DungeonUtils.isMage()) theme.success else theme.danger, dungeonPanel
        )
    }

    private fun createPetInfo() {
        createSectionHeader("Pet Information")

        val petInfo = createStatsPanel()
        createStatRow("Current Pet", PetTracker.name, theme.accent, petInfo)
        createStatRow("Pet Level", PetTracker.level.toString(), theme.accent2, petInfo)
        createStatRow("Pet Item", PetTracker.item, theme.accent2, petInfo)
    }

    private fun createEventListenersInfo() {
        createSectionHeader("Event Listeners")
        val eventPanel = createStatsPanel()
        val eventTypes = EventBus.listeners.keys.sortedBy { it.name }
        val totalListeners = EventBus.listeners.values.sumOf { it.size }

        createStatRow("Total Event Types", eventTypes.size.toString(), theme.accent, eventPanel)
        createStatRow("Total Listeners", totalListeners.toString(), theme.success, eventPanel)

        eventTypes.forEach { eventClass ->
            val listeners = EventBus.listeners[eventClass] ?: emptySet()
            val eventName = getFullEventName(eventClass)
            val isExpanded = expandedEvents.contains(eventName)

            val eventRow = createBlock(2f).constrain {
                x = 0.percent()
                y = CramSiblingConstraint(2f)
                width = 100.percent()
                height = 24.pixels()
            }.setColor(theme.element) childOf eventPanel

            eventRow.onMouseEnter {
                animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, theme.elementHover.toConstraint()) }
            }.onMouseLeave {
                animate { setColorAnimation(Animations.OUT_QUAD, 0.15f, theme.element.toConstraint()) }
            }.onMouseClick {
                if (isExpanded) {
                    expandedEvents.remove(eventName)
                } else {
                    expandedEvents.add(eventName)
                }
                updateDebugInfo()
            }

            UIText(if (isExpanded) "▼" else "▶").constrain {
                x = 8.pixels()
                y = CenterConstraint()
                textScale = 0.8.pixels()
            }.setColor(theme.warning).setFontProvider(CustomFontProvider) childOf eventRow

            UIText(eventName).constrain {
                x = 24.pixels()
                y = CenterConstraint()
                textScale = 0.9.pixels()
            }.setColor(theme.accent).setFontProvider(CustomFontProvider) childOf eventRow

            UIText("${listeners.size} listeners").constrain {
                x = 8.pixels(alignOpposite = true)
                y = CenterConstraint()
                textScale = 0.9.pixels()
            }.setColor(theme.accent2).setFontProvider(CustomFontProvider) childOf eventRow

            if (isExpanded && listeners.isNotEmpty()) {
                val listenersContainer = UIContainer().constrain {
                    x = 0.percent()
                    y = CramSiblingConstraint(2f)
                    width = 100.percent()
                    height = ChildHeightConstraint(2f)
                } childOf eventPanel

                listeners.forEachIndexed { index, listener ->
                    val listenerRow = createBlock(3f).constrain {
                        x = 16.pixels()
                        y = CramSiblingConstraint(if (index == 0) 2f else 1f)
                        width = 100.percent() - 32.pixels()
                        height = 20.pixels()
                    }.setColor(Color(theme.element.red + 8, theme.element.green + 8, theme.element.blue + 8, 180)) childOf listenersContainer

                    UIText("${index + 1}. $listener").constrain {
                        x = 8.pixels()
                        y = CenterConstraint()
                        textScale = 0.8.pixels()
                    }.setColor(Color(theme.accent2.red - 20, theme.accent2.green - 20, theme.accent2.blue - 20)).setFontProvider(CustomFontProvider) childOf listenerRow
                }
            }
        }
    }

    private fun getFullEventName(eventClass: Class<*>): String {
        val className = eventClass.name
        return when {
            className.contains("$") -> {
                val parts = className.split("$")
                if (parts.size >= 2) {
                    val parentClass = parts[0].substringAfterLast(".")
                    val innerClass = parts[1]
                    "$parentClass.$innerClass"
                } else {
                    className.substringAfterLast(".")
                }
            }
            else -> className.substringAfterLast(".")
        }
    }

    private fun createStatsPanel(): UIContainer {
        return UIContainer().constrain {
            x = 0.percent()
            y = CramSiblingConstraint(4f)
            width = 100.percent()
            height = ChildHeightConstraint(2f)
        } childOf debugContainer
    }

    private fun createSectionHeader(text: String) {
        val header = UIContainer().constrain {
            x = 0.percent()
            y = CramSiblingConstraint(8f)
            width = 100.percent()
            height = 30.pixels()
        } childOf debugContainer

        UIText(text).constrain {
            x = 0.percent()
            y = CenterConstraint()
            textScale = 1.4.pixels()
        }.setColor(theme.accent).setFontProvider(CustomFontProvider) childOf header

        createBlock(0f).constrain {
            x = 0.percent()
            y = 100.percent() - 1.pixels()
            width = 100.percent()
            height = 1.pixels()
        }.setColor(theme.divider) childOf header
    }

    private fun createStatRow(label: String, value: String, color: Color, parent: UIContainer) {
        val row = createBlock(2f).constrain {
            x = 0.percent()
            y = CramSiblingConstraint(2f)
            width = 100.percent()
            height = 24.pixels()
        }.setColor(theme.element) childOf parent

        UIText(label).constrain {
            x = 8.pixels()
            y = CenterConstraint()
            textScale = 0.9.pixels()
        }.setColor(theme.accent).setFontProvider(CustomFontProvider) childOf row

        UIText(value).constrain {
            x = 8.pixels(alignOpposite = true)
            y = CenterConstraint()
            textScale = 0.9.pixels()
        }.setColor(color).setFontProvider(CustomFontProvider) childOf row
    }
}