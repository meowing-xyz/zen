package xyz.meowing.zen.features.slayers

import xyz.meowing.zen.Zen
import xyz.meowing.zen.api.EntityDetection.getSlayerEntity
import xyz.meowing.zen.config.ConfigDelegate
import xyz.meowing.zen.config.ui.ConfigUI
import xyz.meowing.zen.config.ui.types.ConfigElement
import xyz.meowing.zen.config.ui.types.ElementType
import xyz.meowing.zen.events.RenderEvent
import xyz.meowing.zen.features.Feature
import xyz.meowing.zen.utils.OutlineUtils
import java.awt.Color

@Zen.Module
object SlayerHighlight : Feature("slayerhighlight", true) {
    private val slayerhighlightcolor by ConfigDelegate<Color>("slayerhighlightcolor")
    private val slayerhighlightwidth by ConfigDelegate<Double>("slayerhighlightwidth")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "Slayer highlight", ConfigElement(
                "slayerhighlight",
                "Slayer highlight",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Slayers", "Slayer highlight", "Color", ConfigElement(
                "slayerhighlightcolor",
                "Slayer highlight color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addElement("Slayers", "Slayer highlight", "Width", ConfigElement(
                "slayerhighlightwidth",
                "Slayer highlight width",
                ElementType.Slider(1.0, 10.0, 2.0, false)
            ))
    }

    override fun initialize() {
        register<RenderEvent.EntityModel> { event ->
            if (event.entity == getSlayerEntity()) {
                OutlineUtils.outlineEntity(
                    event = event,
                    color = slayerhighlightcolor,
                    lineWidth = slayerhighlightwidth.toFloat(),
                    shouldCancelHurt = true
                )
            }
        }
    }
}