package meowing.zen.features.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.utils.OutlineUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MovingObjectPosition
import java.awt.Color

@Zen.Module
object EntityHighlight : Feature("entityhighlight") {
    private val entityhighlightplayercolor by ConfigDelegate<Color>("entityhighlightplayercolor")
    private val entityhighlightmobcolor by ConfigDelegate<Color>("entityhighlightmobcolor")
    private val entityhighlightanimalcolor by ConfigDelegate<Color>("entityhighlightanimalcolor")
    private val entityhighlightothercolor by ConfigDelegate<Color>("entityhighlightothercolor")
    private val entityhighlightwidth by ConfigDelegate<Double>("entityhighlightwidth")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlight",
                "Entity highlight",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Entity highlight", "Color", ConfigElement(
                "entityhighlightplayercolor",
                "Player color",
                ElementType.ColorPicker(Color(0, 255, 255, 255))
            ))
            .addElement("General", "Entity highlight", "Color", ConfigElement(
                "entityhighlightmobcolor",
                "Mob color",
                ElementType.ColorPicker(Color(255, 0, 0, 255))
            ))
            .addElement("General", "Entity highlight", "Color", ConfigElement(
                "entityhighlightanimalcolor",
                "Animal color",
                ElementType.ColorPicker(Color(0, 255, 0, 255))
            ))
            .addElement("General", "Entity highlight", "Color", ConfigElement(
                "entityhighlightothercolor",
                "Other entity color",
                ElementType.ColorPicker(Color(255, 255, 255, 255))
            ))
            .addElement("General", "Entity highlight", "Width", ConfigElement(
                "entityhighlightwidth",
                "Entity highlight width",
                ElementType.Slider(1.0, 10.0, 2.0, false)
            ))
    }

    override fun initialize() {
        register<RenderEvent.EntityModel> { event ->
            val entity = event.entity
            val player = player ?: return@register
            if (entity == player || entity.isInvisible || !isEntityUnderCrosshair(entity)) return@register

            OutlineUtils.outlineEntity(
                event,
                getEntityColor(entity),
                entityhighlightwidth.toFloat(),
                false
            )
        }
    }

    private fun isEntityUnderCrosshair(entity: Entity): Boolean {
        val mouseOver = mc.objectMouseOver ?: return false
        return mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mouseOver.entityHit == entity
    }

    private fun getEntityColor(entity: Entity): Color {
        return when (entity) {
            is EntityPlayer -> entityhighlightplayercolor
            is EntityMob -> entityhighlightmobcolor
            is EntityAnimal -> entityhighlightanimalcolor
            else -> entityhighlightothercolor
        }
    }
}