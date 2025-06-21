package meowing.zen.feats.slayers

import meowing.zen.Zen.Companion.mc
import meowing.zen.events.EntityJoinEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.Utils.removeFormatting
import java.util.regex.Pattern

object vengdmg : Feature("vengdmg") {
    private var nametagID = -1

    fun handleNametagUpdate(entityId: Int) {
        nametagID = entityId
    }

    private val veng = Pattern.compile("^\\d+(,\\d+)*ﬗ$")

    init {
        register<EntityJoinEvent> ({ event ->
            if (nametagID == -1) return@register

            TickScheduler.scheduleServer(2) {
                val entityName = event.entity.name?.removeFormatting() ?: return@scheduleServer
                val vengMatch = veng.matcher(entityName)

                if (vengMatch.matches()) {
                    val spawnedEntity = mc.theWorld?.getEntityByID(event.entity.entityId) ?: return@scheduleServer
                    val nametagEntity = mc.theWorld?.getEntityByID(nametagID) ?: return@scheduleServer

                    if (spawnedEntity.getDistanceToEntity(nametagEntity) <= 5) {
                        val numStr = vengMatch.group(0).replace("ﬗ", "").replace(",", "")
                        numStr.toLongOrNull()?.let { num ->
                            if (num > 500000) ChatUtils.addMessage("§c[Zen] §fVeng DMG: §c${vengMatch.group(0).replace("ﬗ", "")}")
                        }
                    }
                }
            }
        })
    }
}