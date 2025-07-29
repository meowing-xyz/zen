package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.feats.Feature
import java.awt.Color

@Zen.Module
object CustomTint : Feature("customtint") {
    val customtintcolor by ConfigDelegate<Color>("customtintcolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Custom tint", ConfigElement(
                "customtint",
                "Custom Damage Tint",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Custom tint", "Color", ConfigElement(
                "customtintcolor",
                "Custom tint color",
                ElementType.ColorPicker(Color(0, 255, 255, 255))
            ))
    }
}