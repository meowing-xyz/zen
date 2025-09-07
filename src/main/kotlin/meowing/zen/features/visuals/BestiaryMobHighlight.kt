package meowing.zen.features.visuals

import meowing.zen.Zen
import meowing.zen.api.EntityDetection.sbMobID
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.MouseEvent
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.OutlineUtils
import net.minecraft.entity.Entity
import net.minecraft.util.MovingObjectPosition
import java.awt.Color

@Zen.Module
object BestiaryMobHighlight : Feature("bestiarymobhighlighter", true) {
    private val trackedMobs = mutableListOf<String>()
    private val highlightcolor by ConfigDelegate<Color>("bestiarymobhighlightcolor")
    private val highlightwidth by ConfigDelegate<Double>("bestiarymobhighlightwidth")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Visuals", "Bestiary Mob Highlight", "Options", ConfigElement(
                "bestiarymobhighlighter",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Visuals", "Bestiary Mob Highlight", "", ConfigElement(
                "",
                null,
                ElementType.TextParagraph("Middle click on a mob in the world to toggle highlighting for it")
            ))
            .addElement("Visuals", "Bestiary Mob Highlight", "Options", ConfigElement(
                "bestiarymobhighlightcolor",
                "Highlight color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addElement("Visuals", "Bestiary Mob Highlight", "Options", ConfigElement(
                "bestiarymobhighlightwidth",
                "Highlight width",
                ElementType.Slider(1.0, 10.0, 2.0, false)
            ))
    }

    override fun initialize() {
        register<RenderEvent.EntityModel> { event ->
            val mob = event.entity.sbMobID ?: return@register
            if (trackedMobs.contains(mob)) {
                OutlineUtils.outlineEntity(event, highlightcolor, highlightwidth.toFloat())
            }
        }

        register<MouseEvent.Click> { event ->
            if (event.event.button == 2) {
                val mob = getTargetEntity() ?: return@register
                val id = mob.sbMobID ?: return@register ChatUtils.addMessage("${Zen.Companion.prefix} §cThis mob could not be identified for the bestiary tracker!")
                if (trackedMobs.contains(id)) {
                    trackedMobs.remove(id)
                    ChatUtils.addMessage("${Zen.Companion.prefix} §cStopped highlighting ${id}!")
                } else {
                    trackedMobs.add(id)
                    ChatUtils.addMessage("${Zen.Companion.prefix} §aStarted highlighting ${id}!")
                }
            }
        }
    }

    private fun getTargetEntity(): Entity? {
        val mouseOver = mc.objectMouseOver ?: return null
        return if (mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) mouseOver.entityHit else null
    }
}