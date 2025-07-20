package meowing.zen.feats.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature

@Zen.Module
object HideFallingBlocks : Feature("hidefallingblocks") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "General", ConfigElement(
                "hidefallingblocks",
                "Hide falling blocks",
                "Cancels the animation of the blocks falling",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<RenderEvent.FallingBlock> { event ->
            event.cancel()
        }
    }
}