package meowing.zen.feats.meowing

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.CommandUtils
import meowing.zen.utils.DataUtils
import net.minecraft.command.ICommandSender

data class Data(val meowcount: Double = 0.0)

@Zen.Module
object meowcount : Feature("meowcount") {
    private val dataUtils = DataUtils("meowcount", Data())

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Meow count", ConfigElement(
                "meowcount",
                "Meow count",
                "Counts how many times you've sent a message with \"meow\" in it",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<ChatEvent.Send> { event ->
            if (event.message.lowercase().contains("meow")) {
                val currentData = dataUtils.getData()
                val newData = currentData.copy(currentData.meowcount + 1.0)
                dataUtils.setData(newData)
            }
        }
    }

    fun getMeowCount(): Double = dataUtils.getData().meowcount
}

@Zen.Command
object MeowCommand : CommandUtils("meowcount", aliases = listOf("zenmeow", "zenmeowcount")) {
    override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
        val count = meowcount.getMeowCount().toInt()
        ChatUtils.addMessage("$prefix §fYou have meowed §b$count §ftimes!")
    }
}