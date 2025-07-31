package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils

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
        register<WorldEvent.Load> {
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