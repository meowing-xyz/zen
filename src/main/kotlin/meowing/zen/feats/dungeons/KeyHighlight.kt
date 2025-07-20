package meowing.zen.feats.dungeons

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Render3D.drawOutlineBox
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.entity.item.EntityArmorStand
import java.awt.Color

@Zen.Module
object KeyHighlight : Feature("keyhighlight", area = "catacombs") {
    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Dungeons", "Keys", ConfigElement(
                "keyhighlight",
                "Key highlight",
                "Highlights the wither/blood key",
                ElementType.Switch(false)
            ))
            .addElement("Dungeons", "Keys", ConfigElement(
                "keyhighlightcolor",
                "Key highlight color",
                null,
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["keyhighlight"] as? Boolean == true }
            ))
            .addElement("Dungeons", "Keys", ConfigElement(
                "keyhighlightwidth",
                "Key highlight width",
                "Width for the key highlight",
                ElementType.Slider(1.0, 10.0, 2.0, false),
                { config -> config["keyhighlight"] as? Boolean == true }
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
                    Zen.config.keyhighlightcolor,
                    Zen.config.keyhighlightwidth.toFloat()
                )
            }
        }
    }
}