package meowing.zen.feats.noclutter

import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature

object noendermantp : Feature("noendermantp") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "General", ConfigElement(
                "noendermantp",
                "No enderman TP",
                "Disables endermen visually teleporting around.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<RenderEvent.EndermanTP> { event ->
            event.cancel()
        }
    }
}