package meowing.zen.features.dungeons

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.features.Feature
import meowing.zen.utils.Render3D.drawOutlineBox
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.item.EntityArmorStand
import java.awt.Color

@Zen.Module
object KeyHighlight : Feature("keyhighlight", area = "catacombs") {
    private val keyhighlightcolor by ConfigDelegate<Color>("keyhighlightcolor")
    private val keyhighlightwidth by ConfigDelegate<Double>("keyhighlightwidth")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Key Highlight", ConfigElement(
                "keyhighlight",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Dungeons", "Key Highlight", "Color", ConfigElement(
                "keyhighlightcolor",
                "Key highlight color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addElement("Dungeons", "Key Highlight", "Width", ConfigElement(
                "keyhighlightwidth",
                "Key highlight width",
                ElementType.Slider(1.0, 10.0, 2.0, false)
            ))
    }

    override fun initialize() {
        register<RenderEvent.LivingEntity.Post> { event ->
            if (event.entity !is EntityArmorStand) return@register
            val name = event.entity.name?.removeFormatting()
            if (name == "Wither Key" || name == "Blood Key") {
                val entity = event.entity
                drawOutlineBox(
                    entity.posX,
                    entity.posY + 1.15,
                    entity.posZ,
                    1f, 1f,
                    keyhighlightcolor,
                    keyhighlightwidth.toFloat()
                )
            }
        }
    }
}