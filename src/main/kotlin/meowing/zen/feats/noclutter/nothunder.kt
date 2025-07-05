package meowing.zen.feats.noclutter

import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.feats.Feature

object nothunder : Feature("nothunder") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "General", ConfigElement(
                "nothunder",
                "Hide thunder",
                "Cancels thunder animation and sound.",
                ElementType.Switch(false)
            ))
    }
}