package meowing.zen.config

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import java.awt.Color

fun ZenConfig(): ConfigUI {
    var configUI = ConfigUI("config")
    Zen.features.forEach { feature ->
        configUI = feature.addConfig(configUI)
    }
    return configUI
}