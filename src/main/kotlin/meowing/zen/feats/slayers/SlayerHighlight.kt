package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.feats.Feature
import meowing.zen.events.RenderEvent
import meowing.zen.utils.OutlineUtils
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import java.awt.Color

@Zen.Module
object SlayerHighlight : Feature("slayerhighlight", true) {
    private var cachedEntity: EntityLivingBase? = null
    private var lastBossId = -1

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Slayers", "General", ConfigElement(
                "slayerhighlight",
                "Slayer highlight",
                "Highlights your slayer boss",
                ElementType.Switch(false)
            ))
            .addElement("Slayers", "General", ConfigElement(
                "slayerhighlightcolor",
                "Slayer highlight color",
                "Slayer highlight color",
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["slayerhighlight"] as? Boolean == true }
            ))
            .addElement("Slayers", "General", ConfigElement(
                "slayerhighlightwidth",
                "Slayer highlight width",
                null,
                ElementType.Slider(1.0, 10.0, 2.0, false),
                { config -> config["slayerhighlight"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        register<RenderEvent.EntityModel> { event ->
            if (!SlayerTimer.isFighting || SlayerTimer.BossId == -1) {
                cachedEntity = null
                lastBossId = -1
                return@register
            }

            if (cachedEntity == null || lastBossId != SlayerTimer.BossId) {
                cachedEntity = Minecraft.getMinecraft().theWorld?.getEntityByID(SlayerTimer.BossId) as? EntityLivingBase
                lastBossId = SlayerTimer.BossId
            }

            if (event.entity == cachedEntity)
                OutlineUtils.outlineEntity(
                    event = event,
                    color = Zen.config.slayerhighlightcolor,
                    lineWidth = Zen.config.slayerhighlightwidth.toFloat(),
                    shouldCancelHurt = true
                )
        }
    }
}