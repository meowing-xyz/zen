package meowing.zen.api

import meowing.zen.Zen
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.events.EventBus.post
import meowing.zen.events.SkyblockEvent
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.util.Vec3

@Zen.Module
object DamageAPI {
    private val damageRegex = "^[✧✯]?(\\d{1,3}(?:,\\d{3})*)[⚔+✧❤♞☄✷ﬗ✯]*$".toRegex()

    init {
        EventBus.register<EntityEvent.Spawn> { event ->
            val packet = event.packet
            if (packet.entityType != 30) return@register

            val originalName = event.name
            val name = originalName.removeFormatting()
            val matchResult = damageRegex.find(name) ?: return@register

            val damageStr = matchResult.groupValues[1].replace(",", "")
            val damage = damageStr.toIntOrNull() ?: return@register

            val entityPos = Vec3(packet.x / 32.0, packet.y / 32.0, packet.z / 32.0)

            if (post(SkyblockEvent.DamageSplash(damage, originalName, entityPos, packet))) event.cancel()
        }
    }
}