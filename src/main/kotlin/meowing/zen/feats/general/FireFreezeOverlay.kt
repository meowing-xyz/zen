package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.events.SkyblockEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ItemUtils.isHolding
import meowing.zen.utils.Render3D
import meowing.zen.utils.TimeUtils
import meowing.zen.utils.TimeUtils.millis
import net.minecraft.util.Vec3
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

@Zen.Module
object FireFreezeOverlay : Feature("firefreezeoverlay") {
    private var activatedPos: Vec3? = null
    private var activatedAt = TimeUtils.now

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Fire freeze overlay", ConfigElement(
                "firefreezeoverlay",
                "Fire Freeze Overlay",
                "Shows the overlay around the area that will be affected by your fire freeze staff",
                ElementType.Switch(false)
            ))
            .addElement("General", "Fire freeze overlay", ConfigElement(
                "firefreezeoverlaycolor",
                "Fire Freeze Overlay color",
                "The color for the overlay, the border will be a bit darker than this color.",
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["firefreezeoverlay"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<SkyblockEvent.ItemAbilityUsed> { event ->
            if (isHolding("FIRE_FREEZE_STAFF")) {
                activatedPos = mc.thePlayer.positionVector
                activatedAt = TimeUtils.now
            }
        }

        register<RenderEvent.World> { event ->
            if (activatedAt.isZero || activatedPos == null) return@register
            val pos = activatedPos!!
            val remainingTime = (activatedAt + 5.seconds).until.millis
            if (remainingTime < 0) return@register
            val text = "§b${"%.2f".format(remainingTime / 1000.0)}s"

            Render3D.drawFilledCircle(
                pos,
                5f,
                72,
                config.firefreezeoverlaycolor.darker(),
                config.firefreezeoverlaycolor,
                event.partialTicks
            )

            Render3D.drawString(
                text,
                pos.addVector(0.0, 1.0, 0.0),
                event.partialTicks
            )
        }
    }
}