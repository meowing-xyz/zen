package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ItemUtils.skyblockID
import meowing.zen.utils.Render3D
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import java.awt.Color

@Zen.Module
object EffectiveAreaOverlay : Feature("effectiveareaoverlay", true) {
    val items = listOf(
        "BAT_WAND",
        "STARRED_BAT_WAND",
        "HYPERION",
        "ASTRAEA",
        "SCYLLA",
        "VALKYRIE"
    )

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Effective Area Overlay", ConfigElement(
                "effectiveareaoverlay",
                "Custom tint",
                "Renders a filled circle its effective area.",
                ElementType.Switch(false)
            ))
            .addElement("General", "Effective Area Overlay", ConfigElement(
                "effectiveareaoverlaycolor",
                "Colorpicker",
                "Color for the filled circle that renders",
                ElementType.ColorPicker(Color(0, 255, 255, 255)),
                { config -> config["customtint"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<RenderEvent.World> { event ->
            val held = player?.heldItem?.skyblockID ?: return@register
            if (held in items) {
                val lookingAt = player.rayTrace(if (held == "BAT_WAND" || held == "STARRED_BAT_WAND") 45.0 else 9.0, event.partialTicks)
                if (lookingAt.blockPos != null && lookingAt.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    Render3D.drawFilledCircle(
                        Vec3(lookingAt.blockPos.add(0.5, 1.0, 0.5)),
                        7f,
                        72,
                        config.effectiveareaoverlaycolor.darker(),
                        config.effectiveareaoverlaycolor,
                        event.partialTicks
                    )
                }
            }
        }
    }
}