package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.events.EntityEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.OutlineUtils
import meowing.zen.utils.Utils.removeFormatting

object boxstarmobs : Feature("boxstarmobs") {
    private val entities = mutableListOf<Int>()

    override fun initialize() {
        register<EntityEvent.Spawn> { event ->
            if (event.packet.entityType != 30) return@register
            val packet = event.packet
            packet.func_149027_c().find { it.objectType == 4 }?.let {
                val name = it.`object`.toString().removeFormatting()
                if (name.contains("✯ ")) {
                    val id = packet.entityID
                    val offset = if (name.contains("Withermancer")) 3 else 1
                    entities.add(id - offset)
                }
            }
        }

        register<WorldEvent.Change> {
            entities.clear()
        }

        register<RenderEvent.EntityModel> { event ->
            val entity = event.entity
            if (!entities.contains(entity.entityId)) return@register

            OutlineUtils.outlineEntity(
                event,
                Zen.config.boxstarmobscolor,
                Zen.config.boxstarmobswidth.toFloat(),
                false
            )
        }
    }
}