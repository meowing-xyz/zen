package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature

@Zen.Module
object RemoveChatLimit : Feature("removechatlimit") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("QoL", "Remove chat history limit", ConfigElement(
                "removechatlimit",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }
}