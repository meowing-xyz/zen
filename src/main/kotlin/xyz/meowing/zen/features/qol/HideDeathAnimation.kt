package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.EntityEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object HideDeathAnimation : Feature("hidedeathanimation") {
    private val regex = "^\\w+ Livid$".toRegex()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("QoL", "Hide death animation", ConfigElement(
                "hidedeathanimation",
                "Hide death animation",
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<EntityEvent.Leave> { event ->
            if (!regex.matches(event.entity.name.removeFormatting())) event.entity.setDead()
        }
    }
}