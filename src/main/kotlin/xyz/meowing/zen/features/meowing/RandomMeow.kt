package xyz.meowing.zen.features.meowing

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.features.Timer
import xyz.meowing.zen.utils.ChatUtils
import xyz.meowing.zen.utils.TitleUtils
import xyz.meowing.zen.utils.Utils
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