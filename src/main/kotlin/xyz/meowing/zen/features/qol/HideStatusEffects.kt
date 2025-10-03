package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature

@Zen.Module
object HideStatusEffects : Feature("hidestatuseffects") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("QoL", "Hide status effects", ConfigElement(
                "hidestatuseffects",
                "Hide status effects",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }
}