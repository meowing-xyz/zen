package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.feats.Feature
import meowing.zen.events.RenderEntityModelEvent
import meowing.zen.utils.OutlineUtils
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase

object slayerhighlight : Feature("slayerhighlight") {
    private var cachedEntity: EntityLivingBase? = null
    private var lastBossId = -1

    override fun initialize() {
        register<RenderEntityModelEvent> { event ->
            if (!slayertimer.isFighting || slayertimer.BossId == -1) {
                cachedEntity = null
                lastBossId = -1
                return@register
            }

            if (cachedEntity == null || lastBossId != slayertimer.BossId) {
                cachedEntity = Minecraft.getMinecraft().theWorld?.getEntityByID(slayertimer.BossId) as? EntityLivingBase
                lastBossId = slayertimer.BossId
            }

            if (event.entity == cachedEntity)
                OutlineUtils.outlineEntity(
                    event = event,
                    color = Zen.config.slayerhighlightcolor,
                    lineWidth = Zen.config.slayerhighlightwidth,
                    shouldCancelHurt = true
                )
        }
    }
}