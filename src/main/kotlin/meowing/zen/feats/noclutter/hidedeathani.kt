package meowing.zen.feats.noclutter

import meowing.zen.events.EntityEvent
import meowing.zen.feats.Feature

object hidedeathani : Feature("hidedeathanimation") {
    override fun initialize() {
        register<EntityEvent.Leave> { event ->
            event.entity.setDead()
        }
    }
}