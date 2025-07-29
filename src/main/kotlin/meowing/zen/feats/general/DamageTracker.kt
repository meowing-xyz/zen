package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object DamageTracker : Feature("damagetracker") {
    private val regex = "[✧✯]?(\\d{1,3}(?:,\\d{3})*[⚔+✧❤♞☄✷ﬗ✯]*)".toRegex()

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
                val clean = name.removeFormatting()
                if (clean.matches(regex)) ChatUtils.addMessage("$prefix $name")
            }
        }
    }
}