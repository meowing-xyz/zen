package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.features.Feature

@Zen.Module
object RemoveChatLimit : Feature("removechatlimit") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Remove chat history limit", ConfigElement(
                    "removechatlimit",
                    null,
                    ElementType.Switch(false)
            ), isSectionToggle = true)
    }
}