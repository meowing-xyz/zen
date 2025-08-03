package meowing.zen.features.rift

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ItemUtils.isHolding
import meowing.zen.utils.Render3D
import java.awt.Color

@Zen.Module
object CoherentRodOverlay : Feature("coherentrodoverlay", area = "the rift") {
    private val coherentrodoverlaycolor by ConfigDelegate<Color>("coherentrodoverlaycolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Rift", "Coherent Rod Overlay", ConfigElement(
                "coherentrodoverlay",
                "Coherent Rod Overlay",
                ElementType.Switch(false),
            ), isSectionToggle = true)
            .addElement("Rift", "Coherent Rod Overlay", "Color", ConfigElement(
                "coherentrodoverlaycolor",
                "Color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }

    override fun initialize() {
        register<RenderEvent.World> { event ->
            if (isHolding("NEARLY_COHERENT_ROD")) {
                val player = player ?: return@register
                val color = coherentrodoverlaycolor
                Render3D.drawFilledCircle(
                    player.positionVector,
                    8f,
                    72,
                    color.darker(),
                    color,
                    event.partialTicks
                )
            }
        }
    }
}