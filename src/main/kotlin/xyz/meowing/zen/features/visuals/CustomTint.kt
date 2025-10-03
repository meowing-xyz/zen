package xyz.meowing.zen.features.visuals

import xyz.meowing.zen.Zen
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.features.Feature
import java.awt.Color

@Zen.Module
object CustomTint : Feature("customtint") {
    val customtintcolor by ConfigDelegate<Color>("customtintcolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Visuals", "Custom tint", ConfigElement(
                "customtint",
                "Custom Damage Tint",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Visuals", "Custom tint", "Color", ConfigElement(
                "customtintcolor",
                "Custom tint color",
                ElementType.ColorPicker(Color(0, 255, 255, 255))
            ))
    }
}