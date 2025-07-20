package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EntityEvent
import meowing.zen.events.EventBus
import meowing.zen.utils.Utils.removeFormatting

@Zen.Module
object MetadataHandler {
    init {
        EventBus.register<EntityEvent.Metadata> ({ event ->
            if (!Zen.config.slayertimer && !Zen.config.vengdmg && !Zen.config.lasertimer) return@register
            val world = mc.theWorld ?: return@register
            val player = mc.thePlayer ?: return@register

            event.packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
                val name = (obj.`object` as String).removeFormatting()
                if (name.contains("Spawned by") && name.endsWith("by: ${player.name}")) {
                    val targetEntity = world.getEntityByID(event.packet.entityId)
                    val hasBlackhole = targetEntity?.let { entity ->
                        world.loadedEntityList.any { other ->
                            other.name?.removeFormatting()?.lowercase()?.contains("black hole") == true && entity.getDistanceToEntity(other) <= 3f
                        }
                    } ?: false

                    if (hasBlackhole) return@register
                    if (Zen.config.slayertimer) SlayerTimer.handleBossSpawn(event.packet.entityId)
                    if (Zen.config.vengdmg) VengDamage.handleNametagUpdate(event.packet.entityId)
                    if (Zen.config.lasertimer) LaserTimer.handleSpawn(event.packet.entityId)
                }
            }
        })
    }
}