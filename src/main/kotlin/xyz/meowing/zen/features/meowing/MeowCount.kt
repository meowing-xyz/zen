package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.ChatEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.DataUtils
import xyz.meowing.knit.api.command.Commodore

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
object MeowCommand : Commodore("meowcount", "zenmeow", "zenmeowcount") {
    init {
        runs {
            val count = MeowCount.getMeowCount().toInt()
            ChatUtils.addMessage("$prefix §fYou have meowed §b$count §ftimes!")
        }
    }
}