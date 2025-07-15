package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.HurtCamEvent
import meowing.zen.feats.Feature

@Zen.Module
object nohurtcam : Feature("nohurtcam") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "No hurt cam", ConfigElement(
                "nohurtcam",
                "Disable hurt camera",
                "Disables the camera bob when you get hit",
                ElementType.Switch(false)
            ))
    }

    override fun initialize() {
        register<HurtCamEvent> { event ->
            event.cancel()
        }
    }
}