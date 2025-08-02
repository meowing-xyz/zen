package meowing.zen.features.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.features.Feature

@Zen.Module
object HideStatusEffects : Feature("hidestatuseffects") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "Hide status effects", ConfigElement(
                "hidestatuseffects",
                "Hide status effects",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }
}