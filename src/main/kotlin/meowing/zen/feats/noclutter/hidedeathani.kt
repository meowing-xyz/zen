package meowing.zen.feats.noclutter

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.feats.Feature

@Zen.Module
object hidedeathani : Feature("hidedeathanimation") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("No clutter", "General", ConfigElement(
                "hidedeathanimation",
                "Hide death animation",
                "Cancels the death animation of mobs.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<EntityEvent.Leave> { event ->
            event.entity.setDead()
        }
    }
}