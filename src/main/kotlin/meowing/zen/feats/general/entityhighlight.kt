package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.RenderEntityModelEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.OutlineUtils
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MovingObjectPosition

object entityhighlight : Feature("entityhighlight") {
    override fun initialize() {
        register<RenderEntityModelEvent> { event ->
            val entity = event.entity
            if (entity == mc.thePlayer || entity.isInvisible) return@register

            val mouseOver = mc.objectMouseOver
            if (mouseOver?.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY || mouseOver.entityHit != entity) return@register

            val color = Zen.config.entityhighlightcolor
            event.entity.canEntityBeSeen(mc.thePlayer)
            OutlineUtils.outlineEntity(
                event = event,
                color = color,
                lineWidth = Zen.config.entityhighlightwidth,
                shouldCancelHurt = false
            )
        }
    }
}