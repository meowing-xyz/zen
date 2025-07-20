package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.feats.Feature

@Zen.Module
object RemoveChatLimit : Feature("removechatlimit") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement(
                "General", "Chat history", ConfigElement(
                    "removechatlimit",
                    "Remove chat history limit",
                    "Removes the limit from your chat history.",
                    ElementType.Switch(false)
                )
            )
    }
}