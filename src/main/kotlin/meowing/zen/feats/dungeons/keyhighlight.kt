package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.mixins.AccessorMinecraft
import meowing.zen.utils.RenderUtils.drawOutlineBox
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.client.event.RenderLivingEvent

object keyhighlight {
    val mc = Minecraft.getMinecraft()
    @JvmStatic
    fun initialize() {
        Zen.registerListener("keyhighlight", this)
    }

    @SubscribeEvent
    fun onEntityRender(event: RenderLivingEvent.Post<EntityArmorStand>) {
        if (event.entity is EntityArmorStand) {
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
