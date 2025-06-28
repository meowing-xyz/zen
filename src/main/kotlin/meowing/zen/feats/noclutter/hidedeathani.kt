package meowing.zen.feats.noclutter

import meowing.zen.events.EntityLeaveEvent
import meowing.zen.feats.Feature

object hidedeathani : Feature("hidedeathanimation") {
    override fun initialize() {
        register<EntityLeaveEvent> { event ->
            event.entity.setDead()
        }
    }
}