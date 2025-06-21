package meowing.zen.feats.slayers

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.*
import meowing.zen.utils.Utils.removeFormatting

object MetadataHandler {
    init {
        EventBus.register<EntityMetadataUpdateEvent> ({ event ->
            val world = mc.theWorld ?: return@register
            val player = mc.thePlayer ?: return@register

            event.packet.func_149376_c()?.find { it.dataValueId == 2 && it.`object` is String }?.let { obj ->
                val name = (obj.`object` as String).removeFormatting()
                if (name.contains("Spawned by") && name.endsWith("by: ${player.name}")) {
                    val entity = world.getEntityByID(event.packet.entityId) ?: return@let
                    val hasBlackhole = world.loadedEntityList.any {
                        it.name?.removeFormatting()?.lowercase()?.contains("black hole") == true && entity.getDistanceToEntity(it) <= 3
                    }

                    if (!hasBlackhole) {
                        slayertimer.handleBossSpawn(event.packet.entityId)
                        vengdmg.handleNametagUpdate(event.packet.entityId)
                    }
                }
            }
        })
    }
}