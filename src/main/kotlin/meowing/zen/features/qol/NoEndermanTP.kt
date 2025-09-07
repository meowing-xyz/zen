package meowing.zen.features.qol

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature

@Zen.Module
object NoEndermanTP : Feature("noendermantp") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("QoL", "No enderman TP", ConfigElement(
                "noendermantp",
                "No enderman TP",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<RenderEvent.EndermanTP> { event ->
            event.cancel()
        }
    }
}