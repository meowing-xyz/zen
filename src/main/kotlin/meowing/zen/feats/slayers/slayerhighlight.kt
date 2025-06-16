package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.feats.slayers.slayertimer.BossId
import meowing.zen.feats.slayers.slayertimer.isFighting
import meowing.zen.events.RenderEntityModelEvent
import meowing.zen.utils.OutlineUtils
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object slayerhighlight {
    private var cachedEntity: Entity? = null
    private var lastBossId = -1

    @JvmStatic
    fun initialize() {
        Zen.registerListener("slayerhighlight", this)
    }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (!isFighting || BossId == -1) {
            cachedEntity = null
            lastBossId = -1
            return
        }

        if (cachedEntity == null || lastBossId != BossId) {
            cachedEntity = Minecraft.getMinecraft().theWorld?.getEntityByID(BossId)
            lastBossId = BossId
        }

        val targetEntity = cachedEntity as? EntityLivingBase ?: return
        if (event.entity == targetEntity) {
            OutlineUtils.outlineEntity(
                event = event,
                color = Zen.config.slayerhighlightcolor,
                lineWidth = Zen.config.slayerhighlightwidth,
                depth = true,
                shouldCancelHurt = true
            )
        }
    }
}