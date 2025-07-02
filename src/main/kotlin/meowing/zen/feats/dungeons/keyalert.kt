package meowing.zen.feats.dungeons

import meowing.zen.events.ChatEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.events.EntityEvent
import net.minecraft.entity.item.EntityArmorStand

object keyalert : Feature("keyalert", area = "catacombs") {
    private var bloodOpen = false

    override fun initialize() {
        register<ChatEvent.Receive> { event ->
            if (!bloodOpen && event.event.message.unformattedText.removeFormatting().startsWith("[BOSS] The Watcher: ")) bloodOpen = true
        }

        register<EntityEvent.Join> { event ->
            if (bloodOpen) return@register
            if (event.entity !is EntityArmorStand) return@register
            TickUtils.scheduleServer(2) {
                val name = event.entity.name?.removeFormatting() ?: return@scheduleServer
                when {
                    name.contains("Wither Key") -> Utils.showTitle("§8Wither §fkey spawned!", "", 40)
                    name.contains("Blood Key") -> Utils.showTitle("§cBlood §fkey spawned!", "", 40)
                }
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
