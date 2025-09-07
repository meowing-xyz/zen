package meowing.zen.features.visuals

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.utils.ItemUtils.skyblockID
import meowing.zen.utils.Render3D
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import java.awt.Color

@Zen.Module
object EffectiveAreaOverlay : Feature("effectiveareaoverlay", true) {
    private val items = listOf(
        "BAT_WAND",
        "STARRED_BAT_WAND",
        "HYPERION",
        "ASTRAEA",
        "SCYLLA",
        "VALKYRIE"
    )
    private val effectiveareaoverlaycolor by ConfigDelegate<Color>("effectiveareaoverlaycolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Visuals", "Effective Area Overlay", ConfigElement(
                "effectiveareaoverlay",
                "Effective Area Overlay",
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Visuals", "Effective Area Overlay", "Color", ConfigElement(
                "effectiveareaoverlaycolor",
                "Color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }

    override fun initialize() {
        register<RenderEvent.World> { event ->
            val held = player?.heldItem?.skyblockID ?: return@register
            if (held in items) {
                val lookingAt = player?.rayTrace(if (held == "BAT_WAND" || held == "STARRED_BAT_WAND") 45.0 else 9.0, event.partialTicks)
                if (lookingAt?.blockPos != null && lookingAt.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    Render3D.drawFilledCircle(
                        Vec3(lookingAt.blockPos.add(0.5, 1.0, 0.5)),
                        7f,
                        72,
                        effectiveareaoverlaycolor.darker(),
                        effectiveareaoverlaycolor,
                        event.partialTicks
                    )
                }
            }
        }
    }
}