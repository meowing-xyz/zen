package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.OutlineUtils
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
            if (entity == mc.thePlayer || entity.isInvisible) return@register

            val mouseOver = mc.objectMouseOver
            if (mouseOver?.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY || mouseOver.entityHit != entity) return@register

            val color = when (entity) {
                is EntityPlayer -> Zen.config.entityhighlightplayercolor
                is EntityMob -> Zen.config.entityhighlightmobcolor
                is EntityAnimal -> Zen.config.entityhighlightanimalcolor
                else -> Zen.config.entityhighlightothercolor
            }

            event.entity.canEntityBeSeen(mc.thePlayer)
            OutlineUtils.outlineEntity(
                event = event,
                color = color,
                lineWidth = Zen.config.entityhighlightwidth.toFloat(),
                shouldCancelHurt = false
            )
        }
    }
}