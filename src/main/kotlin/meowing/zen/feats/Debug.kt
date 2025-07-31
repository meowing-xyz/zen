package meowing.zen.feats

import meowing.zen.UpdateGUI
import meowing.zen.Zen
import meowing.zen.Zen.Companion.features
import meowing.zen.Zen.Companion.mc
import meowing.zen.Zen.Companion.prefix
import meowing.zen.api.PlayerStats
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.utils.*
import net.minecraft.command.ICommandSender
import java.awt.Color

@Zen.Module
object Debug : Feature() {
    data class PersistentData(var debugmode: Boolean = false)
    val data = DataUtils("Debug", PersistentData())

    inline val debugmode get() = data.getData().debugmode

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        if (!debugmode) {
            return configUI
        }

        return configUI
            .addElement("Debug", "Config Test", "Switch", ConfigElement(
                "test_switch",
                "Switch test",
                ElementType.Switch(false)
            ))
            .addElement("Debug", "Config Test", "Button", ConfigElement(
                "test_button",
                "Button test",
                ElementType.Button("Click Me!") { configData, configUI ->
                    println("Button clicked!")
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
                ChatUtils.addMessage("$prefix §fToggled dev mode. You will need to restart to see the difference in the Config UI")
            }
            "stats" -> {
                ChatUtils.addMessage(
                    "§cHealth: ${PlayerStats.health} | Max: ${PlayerStats.maxHealth} | §6Absorb: ${PlayerStats.absorption}\n" +
                            "§9Mana: ${PlayerStats.mana} | Max: ${PlayerStats.maxMana} | §3Overflow: ${PlayerStats.overflowMana}\n" +
                            "§dRift Time: ${PlayerStats.riftTimeSeconds} | Max: ${PlayerStats.maxRiftTime}\n" +
                            "§aDefense: ${PlayerStats.defense} | Effective: ${PlayerStats.effectiveHealth} | Effective Max: ${PlayerStats.maxEffectiveHealth}"
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
                features.forEach { it ->
                    if (it.isEnabled()) ChatUtils.addMessage("§f> §c${it.configKey}")
                }
            }
            else -> {
                ChatUtils.addMessage("$prefix §fUsage: §7/§bzendebug §c<toggle|stats|dgutils|info>")
            }
        }
    }
}