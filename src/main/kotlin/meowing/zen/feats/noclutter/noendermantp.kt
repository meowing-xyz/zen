package meowing.zen.feats.noclutter

import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature

object noendermantp : Feature("noendermantp") {
    override fun initialize() {
        register<RenderEvent.EndermanTP> { event ->
            event.cancel()
        }
    }
}