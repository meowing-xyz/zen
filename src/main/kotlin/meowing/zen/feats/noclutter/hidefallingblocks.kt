package meowing.zen.feats.noclutter

import meowing.zen.events.RenderFallingBlockEvent
import meowing.zen.feats.Feature

object hidefallingblocks : Feature("hidefallingblocks") {
    override fun initialize() {
        register<RenderFallingBlockEvent> { event ->
            event.cancel()
        }
    }
}