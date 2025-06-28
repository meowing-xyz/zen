package meowing.zen.feats.noclutter

import meowing.zen.events.EntityJoinEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.TickUtils
import meowing.zen.utils.Utils.removeFormatting

object hidedamage : Feature("hidedamage", area = "catacombs") {
    private val regex = "^.?\\d[\\d,.]+.*?$".toRegex()
    override fun initialize() {
        register<EntityJoinEvent> { event ->
            TickUtils.scheduleServer(1) {
                if (regex.matches(event.entity.name.removeFormatting())) event.entity.setDead()
            }
        }
    }
}