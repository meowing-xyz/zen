package meowing.zen.features.meowing

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.features.Feature
import meowing.zen.features.Timer
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TitleUtils
import meowing.zen.utils.Utils
import kotlin.random.Random

@Zen.Module
object RandomMeow : Feature("randommeow") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Meowing", "Random Meows", ConfigElement(
                "randommeow",
                null,
                ElementType.Switch(true)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        setupLoops {
            loopDynamic<Timer>({ Random.nextLong(3600000, 21600000) }) {
                ChatUtils.addMessage("$prefix §dmeow.")
                Utils.playSound("mob.cat.meow", 1f, 1f)
                TitleUtils.showTitle("§dmeow.", null, 2000)
            }
        }
    }
}