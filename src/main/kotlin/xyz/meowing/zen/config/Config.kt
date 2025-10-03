package xyz.meowing.zen.config

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import java.awt.Color

fun ZenConfig(): ConfigUI {
    var configUI = ConfigUI("config")
    Zen.features.forEach { feature ->
        configUI = feature.addConfig(configUI)
    }
    return configUI
}