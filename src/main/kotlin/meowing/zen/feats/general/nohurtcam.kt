package meowing.zen.feats.general

import meowing.zen.events.HurtCamEvent
import meowing.zen.feats.Feature

object nohurtcam : Feature("nohurtcam") {
    override fun initialize() {
        register<HurtCamEvent> { event ->
            event.cancel()
        }
    }
}