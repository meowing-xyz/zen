package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature

@Zen.Module
object HideThunder : Feature("hidethunder") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("QoL", "Hide thunder", ConfigElement(
                "hidethunder",
                "Hide thunder",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }
}