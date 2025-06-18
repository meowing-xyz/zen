package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.utils.RenderUtils.drawOutlineBox
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraft.entity.item.EntityArmorStand

object keyhighlight {
    @JvmStatic
    fun initialize() {
        Zen.registerListener("keyhighlight", this)
    }

    @SubscribeEvent
    fun onWorldRender(event: net.minecraftforge.client.event.RenderWorldLastEvent) {
        val world = Minecraft.getMinecraft().theWorld ?: return

        for (entity in world.loadedEntityList) {
            if (entity !is EntityArmorStand) continue
            val name = entity.name?.removeFormatting() ?: continue
            if (name != "Wither Key" && name != "Blood Key") continue
            drawOutlineBox(entity, Zen.config.keyhighlightcolor, event.partialTicks, Zen.config.keyhighlightwidth)
        }
    }
}
