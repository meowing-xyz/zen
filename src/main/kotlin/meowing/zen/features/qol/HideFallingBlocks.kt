package meowing.zen.features.qol

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature

@Zen.Module
object HideFallingBlocks : Feature("hidefallingblocks") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("QoL", "Hide falling blocks", ConfigElement(
                "hidefallingblocks",
                "Hide falling blocks",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<RenderEvent.FallingBlock> { event ->
            event.cancel()
        }
    }
}