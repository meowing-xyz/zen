package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature

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
}