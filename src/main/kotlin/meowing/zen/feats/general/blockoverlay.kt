package meowing.zen.feats.general

import com.sun.org.apache.xpath.internal.operations.Bool
import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.RenderUtils.renderBlock
import net.minecraft.init.Blocks
import scala.tools.nsc.doc.base.comment.Bold
import java.awt.Color

object blockoverlay : Feature("blockoverlay") {
    private val excludedBlocks = setOf(Blocks.air, Blocks.flowing_lava, Blocks.lava, Blocks.flowing_water, Blocks.water)

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Block overlay", ConfigElement(
                "blockoverlay",
                "Block overlay",
                "Highlights your block with custom color",
                ElementType.Switch(false)
            ))
            .addElement("General", "Block overlay", ConfigElement(
                "blockoverlayfill",
                "Filled block overlay",
                "Enable to render filled block overlay",
                ElementType.Switch(false),
                { config -> config["blockoverlay"] as? Boolean == true }
            ))
            .addElement("General", "Block overlay", ConfigElement(
                "blockoverlaycolor",
                "Block overlay color",
                null,
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["blockoverlay"] as? Boolean == true }
            ))
            .addElement("General", "Block overlay", ConfigElement(
                "blockoverlaywidth",
                "Block overlay width",
                null,
                ElementType.Slider(1.0, 10.0, 2.0, false),
                { config -> config["blockoverlay"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        var fill = false

        Zen.registerCallback("blockoverlayfill") { newval ->
            fill = newval as Boolean
        }
        register<RenderEvent.BlockHighlight> { event ->
            val block = event.blockPos.let { mc.theWorld.getBlockState(it).block }
            if (block !in excludedBlocks) {
                event.cancel()
                renderBlock(
                    event.blockPos,
                    event.partialTicks,
                    Zen.config.blockoverlayfill,
                    Zen.config.blockoverlaycolor,
                    Zen.config.blockoverlaywidth.toFloat()
                )
            }
        }
    }
}