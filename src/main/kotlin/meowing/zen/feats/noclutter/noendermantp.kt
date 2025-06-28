package meowing.zen.feats.noclutter

import meowing.zen.events.EndermanTPEvent
import meowing.zen.feats.Feature

object noendermantp : Feature("noendermantp") {
    override fun initialize() {
        register<EndermanTPEvent> { event ->
            event.cancel()
        }
    }
}