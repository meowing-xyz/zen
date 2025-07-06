package meowing.zen.feats.dungeons

import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.events.EntityEvent
import net.minecraft.entity.item.EntityArmorStand

object keyalert : Feature("keyalert", area = "catacombs") {
    private var bloodOpen = false

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Keys", ConfigElement(
                "keyalert",
                "Key spawn alert",
                "Displays a title when the wither/blood key spawns",
                ElementType.Switch(false)
            ))
    }
    
    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (!bloodOpen && event.event.message.unformattedText.removeFormatting().startsWith("[BOSS] The Watcher: ")) bloodOpen = true
        }

        register<EntityEvent.Spawn> { event ->
            if (bloodOpen) return@register
            if (event.packet.entityType != 30) return@register
            val name = event.packet.func_149027_c().find{ it.objectType == 4 }?.`object`?.toString() ?: return@register
            when {
                name.contains("Wither Key") -> Utils.showTitle("§8Wither §fkey spawned!", "", 40)
                name.contains("Blood Key") -> Utils.showTitle("§cBlood §fkey spawned!", "", 40)
            }
        }
    }

    override fun onRegister() {
        bloodOpen = false
    }

    override fun onUnregister() {
        bloodOpen = false
    }
}
