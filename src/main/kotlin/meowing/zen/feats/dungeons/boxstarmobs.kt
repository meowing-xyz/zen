package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.WorldEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.OutlineUtils
import meowing.zen.utils.Utils.removeFormatting
import java.awt.Color

object boxstarmobs : Feature("boxstarmobs") {
    private val entities = mutableListOf<Int>()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Box star mobs", ConfigElement(
                "boxstarmobs",
                "Box star mobs",
                "Highlights star mobs in dungeons.",
                ElementType.Switch(false)
            ))
            .addElement("Dungeons", "Box star mobs", ConfigElement(
                "boxstarmobscolor",
                "Box star mobs color",
                null,
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["boxstarmobs"] as? Boolean == true }
            ))
            .addElement("Dungeons", "Box star mobs", ConfigElement(
                "boxstarmobswidth",
                "Box star mobs width",
                "Width for starred mob's outline",
                ElementType.Slider(1.0, 5.0, 2.0, false),
                { config -> config["boxstarmobs"] as? Boolean == true }
            ))
    }

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