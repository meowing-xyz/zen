package meowing.zen.feats.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature

@Zen.Module
object HideFog : Feature("hidefog") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "General", ConfigElement(
                "hidefog",
                "Hide fog",
                "Hides fog during rendering. \n&c&lRequires a restart to properly toggle",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<RenderEvent.Fog> { event ->
            event.event.density = 0f
            event.cancel()
        }
    }
}