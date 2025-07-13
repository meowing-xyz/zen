package meowing.zen.feats.general

import meowing.zen.Zen.Companion.config
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.OutlineUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MovingObjectPosition
import java.awt.Color

object entityhighlight : Feature("entityhighlight") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlight",
                "Entity highlight",
                "Highlights the entity you are looking at",
                ElementType.Switch(false)
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightplayercolor",
                "Player color",
                "Color for highlighted players",
                ElementType.ColorPicker(Color(0, 255, 255, 255)),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightmobcolor",
                "Mob color",
                "Color for highlighted mobs",
                ElementType.ColorPicker(Color(255, 0, 0, 255)),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightanimalcolor",
                "Animal color",
                "Color for highlighted animals",
                ElementType.ColorPicker(Color(0, 255, 0, 255)),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightothercolor",
                "Other entity color",
                "Color for other highlighted entities",
                ElementType.ColorPicker(Color(255, 255, 255, 255)),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
            .addElement("General", "Entity highlight", ConfigElement(
                "entityhighlightwidth",
                "Entity highlight width",
                "Width of the entity highlight outline",
                ElementType.Slider(1.0, 10.0, 2.0, false),
                { config -> config["entityhighlight"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<RenderEvent.EntityModel> { event ->
            val entity = event.entity
            val player = mc.thePlayer ?: return@register
            if (entity == player || entity.isInvisible || !isEntityUnderCrosshair(entity)) return@register

            OutlineUtils.outlineEntity(
                event,
                getEntityColor(entity),
                config.entityhighlightwidth,
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
            is EntityPlayer -> config.entityhighlightplayercolor
            is EntityMob -> config.entityhighlightmobcolor
            is EntityAnimal -> config.entityhighlightanimalcolor
            else -> config.entityhighlightothercolor
        }
    }
}