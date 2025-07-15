package meowing.zen.feats.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.feats.Feature

@Zen.Module
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