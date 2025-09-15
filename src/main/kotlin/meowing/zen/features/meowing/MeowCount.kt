package meowing.zen.features.meowing

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.DataUtils
import net.minecraft.command.ICommandSender

data class Data(var meowcount: Double = 0.0)

@Zen.Module
object MeowCount : Feature("meowcount") {
    private val dataUtils = DataUtils("meowcount", Data())

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Meow count", ConfigElement(
                "meowcount",
                "Meow count",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Meowing", "Meow count", "", ConfigElement(
                "",
                null,
                ElementType.TextParagraph("Counts how many times you have meowed in chat. You can use the command §c/meowcount §rto check your meow count.")
            ))
    }

    override fun initialize() {
        register<ChatEvent.Send> { event ->
            if (event.message.lowercase().contains("meow")) {
                dataUtils.updateAndSave {
                    meowcount++
                }
            }
        }
    }

    fun getMeowCount(): Double = dataUtils.getData().meowcount
}

@Zen.Command
object MeowCommand : CommandUtils("meowcount", aliases = listOf("zenmeow", "zenmeowcount")) {
    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        val count = MeowCount.getMeowCount().toInt()
        ChatUtils.addMessage("$prefix §fYou have meowed §b$count §ftimes!")
    }
}