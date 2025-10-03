package xyz.meowing.zen.features.general

import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.prefix
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.WorldEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.ChatUtils

@Zen.Module
object WorldAge : Feature("worldagechat") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "World age message", ConfigElement(
                "worldagechat",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<WorldEvent.Change> {
            val currentWorld = it.world
            createTimer(20,
                onComplete = {
                    val daysRaw = currentWorld.worldTime / 24000.0
                    val days = if (daysRaw % 1.0 == 0.0) daysRaw.toInt().toString() else "%.1f".format(daysRaw)
                    ChatUtils.addMessage("$prefix §fWorld is §b$days §fdays old.")
                }
            )
        }
    }
}