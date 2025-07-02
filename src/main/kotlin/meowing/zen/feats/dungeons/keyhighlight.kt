package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.RenderUtils.drawOutlineBox
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.item.EntityArmorStand

object keyhighlight : Feature("keyhighlight", area = "catacombs") {
    override fun initialize() {
        register<RenderEvent.LivingEntity.Post> { event ->
            if (event.entity !is EntityArmorStand) return@register
            val name = event.entity.name?.removeFormatting()
            if (name == "Wither Key" || name == "Blood Key") {
                val entity = event.entity
                drawOutlineBox(
                    entity.posX, entity.posY + 1.15, entity.posZ,
                    1f, 1f,
                    Zen.config.keyhighlightcolor, Zen.config.keyhighlightwidth.toFloat()
                )
            }
        }
    }
}