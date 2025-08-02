package meowing.zen.features.dungeons

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.EntityEvent
import meowing.zen.events.RenderEvent
import meowing.zen.events.WorldEvent
import meowing.zen.features.Feature
import meowing.zen.utils.OutlineUtils
import meowing.zen.utils.Utils.removeFormatting
import java.awt.Color

@Zen.Module
object HighlightStarMobs : Feature("boxstarmobs", area = "catacombs") {
    private val entities = mutableListOf<Int>()
    private val boxstarmobscolor by ConfigDelegate<Color>("boxstarmobscolor")
    private val boxstarmobswidth by ConfigDelegate<Double>("boxstarmobswidth")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Highlight star mobs", ConfigElement(
                "boxstarmobs",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Dungeons", "Highlight star mobs", "Color", ConfigElement(
                "boxstarmobscolor",
                "Highlight star mobs color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addElement("Dungeons", "Highlight star mobs", "Width", ConfigElement(
                "boxstarmobswidth",
                "Highlight star mobs width",
                ElementType.Slider(1.0, 5.0, 2.0, false)
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
                boxstarmobscolor,
                boxstarmobswidth.toFloat(),
                false
            )
        }
    }
}