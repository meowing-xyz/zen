package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.feats.Feature
import meowing.zen.utils.RenderUtils.drawOutlineBox
import meowing.zen.utils.Utils.removeFormatting
import meowing.zen.events.RenderLivingEntityPostEvent
import net.minecraft.entity.item.EntityArmorStand

object keyhighlight : Feature("keyhighlight", area = "catacombs") {
    override fun initialize() {
        register<RenderLivingEntityPostEvent> { event ->
            if (event.entity !is EntityArmorStand) return@register
            val name = event.entity.name?.removeFormatting()
            if (name == "Wither Key" || name == "Blood Key") {
                val entity = event.entity
                drawOutlineBox(
                    entity.posX, entity.posY, entity.posZ,
                    entity.width, entity.height,
                    Zen.config.keyhighlightcolor, Zen.config.keyhighlightwidth
                )
            }
        }
    }
}
