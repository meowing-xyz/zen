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

@Zen.Module
object HighlightStarMobs : Feature("boxstarmobs", area = "catacombs") {
    private val entities = mutableListOf<Int>()

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Highlight star mobs", ConfigElement(
                "boxstarmobs",
                "Highlight star mobs",
                "Highlights star mobs in dungeons.",
                ElementType.Switch(false)
            ))
            .addElement("Dungeons", "Highlight star mobs", ConfigElement(
                "boxstarmobscolor",
                "Highlight star mobs color",
                null,
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["boxstarmobs"] as? Boolean == true }
            ))
            .addElement("Dungeons", "Highlight star mobs", ConfigElement(
                "boxstarmobswidth",
                "Highlight star mobs width",
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
                config.boxstarmobscolor,
                config.boxstarmobswidth.toFloat(),
                false
            )
        }
    }
}