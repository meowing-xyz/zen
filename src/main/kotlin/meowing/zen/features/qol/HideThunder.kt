package meowing.zen.features.qol

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.features.Feature

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