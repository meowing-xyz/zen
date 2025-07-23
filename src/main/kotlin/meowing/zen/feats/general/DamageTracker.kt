package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils

@Zen.Module
object DamageTracker : Feature("damagetracker", true) {
    private val regex = Regex("\\s|^§\\w\\D$")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Damage tracker", ConfigElement(
                "damagetracker",
                "Damage tracker",
                "Sends the damage done by you and other players in a certain area in chat.",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<EntityEvent.Spawn> { event ->
            if (event.packet.entityType != 30) return@register
            event.packet.func_149027_c().find { it.objectType == 4 }?.let {
                val name = it.`object`.toString()
                if (!name.matches(regex)) ChatUtils.addMessage("$prefix $name")
            }
        }
    }
}