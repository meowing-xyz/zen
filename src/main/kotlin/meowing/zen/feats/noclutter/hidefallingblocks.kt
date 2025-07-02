package meowing.zen.feats.noclutter

import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature

object hidefallingblocks : Feature("hidefallingblocks") {
    override fun initialize() {
        register<RenderEvent.FallingBlock> { event ->
            event.cancel()
        }
    }
}