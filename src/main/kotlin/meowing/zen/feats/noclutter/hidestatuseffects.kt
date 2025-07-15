package meowing.zen.feats.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.feats.Feature

@Zen.Module
object hidestatuseffects : Feature("hidestatuseffects") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "General", ConfigElement(
                "hidestatuseffects",
                "Hide status effects",
                "Hides the status effects in your inventory.",
                ElementType.Switch(false)
            ))
    }
}