package xyz.meowing.zen.features.qol

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.HurtCamEvent
import xyz.meowing.zen.features.Feature

@Zen.Module
object NoHurtCam : Feature("nohurtcam") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("QoL", "No hurt cam shake", ConfigElement(
                "nohurtcam",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
    }

    override fun initialize() {
        register<HurtCamEvent> { event ->
            event.cancel()
        }
    }
}