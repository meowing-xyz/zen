package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.*
import meowing.zen.utils.Utils.removeFormatting

object MetadataHandler {
    init {
        EventBus.register<EntityMetadataEvent> ({ event ->
            val world = mc.theWorld ?: return@register
            val player = mc.thePlayer ?: return@register

            event.packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
                val name = (obj.`object` as String).removeFormatting()
                if (name.contains("Spawned by") && name.endsWith("by: ${player.name}")) {
                    val targetEntity = world.getEntityByID(event.packet.entityId)
                    val hasBlackhole = targetEntity?.let { entity ->
                        world.loadedEntityList.any { Entity ->
                            entity.getDistanceToEntity(Entity) <= 3f && Entity.name?.removeFormatting()?.lowercase()?.contains("black hole") == true
                        }
                    } ?: false

                    if (hasBlackhole) return@register
                    if (Zen.config.slayertimer) slayertimer.handleBossSpawn(event.packet.entityId)
                    if (Zen.config.vengdmg) vengdmg.handleNametagUpdate(event.packet.entityId)
                }
            }
        })
    }
}