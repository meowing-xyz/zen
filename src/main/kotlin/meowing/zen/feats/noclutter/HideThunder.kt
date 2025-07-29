package meowing.zen.feats.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.feats.Feature

@Zen.Module
object HideThunder : Feature("hidethunder") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "Hide thunder", ConfigElement(
                "hidethunder",
                "Hide thunder",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }
}