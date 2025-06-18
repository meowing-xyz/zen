package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.utils.Utils
import meowing.zen.utils.Utils.removeFormatting
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.event.world.WorldEvent

object keyalert {
    private var bloodOpen = false

    @JvmStatic
    fun initialize() {
        Zen.registerListener("keyalert", this)
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val msg = event.message.unformattedText
        if (!bloodOpen && msg.startsWith("[BOSS] The Watcher: ")) bloodOpen = true
    }

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (bloodOpen) return
        if (event.entity !is EntityArmorStand) return

        val name = event.entity.name?.removeFormatting() ?: return
        when {
            name.contains("Wither Key") -> Utils.showTitle("§8Wither §fkey spawned!", null, 2000)
            name.contains("Blood Key") -> Utils.showTitle("§cBlood §fkey spawned!", null, 2000)
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        bloodOpen = false
    }
}
