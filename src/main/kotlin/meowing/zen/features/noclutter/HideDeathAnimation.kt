package meowing.zen.features.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.features.Feature
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object HideDeathAnimation : Feature("hidedeathanimation") {
    private val regex = "^\\w+ Livid$".toRegex()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "Hide death animation", ConfigElement(
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