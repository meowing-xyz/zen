package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Render3D.renderBlock
import net.minecraft.init.Blocks
import java.awt.Color

@Zen.Module
object BlockOverlay : Feature("blockoverlay") {
    private val excludedBlocks = setOf(Blocks.air, Blocks.flowing_lava, Blocks.lava, Blocks.flowing_water, Blocks.water)
    private val blockoverlayfill by ConfigDelegate<Boolean>("blockoverlayfill")
    private val blockoverlaycolor by ConfigDelegate<Color>("blockoverlaycolor")
    private val blockoverlaywidth by ConfigDelegate<Double>("blockoverlaywidth")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("General", "Block overlay", ConfigElement(
                "blockoverlay",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("General", "Block overlay", "Color", ConfigElement(
                "blockoverlaycolor",
                "Block overlay color",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
            .addElement("General", "Block overlay", "Options", ConfigElement(
                "blockoverlayfill",
                "Filled block overlay",
                ElementType.Switch(false)
            ))
            .addElement("General", "Block overlay", "Options", ConfigElement(
                "blockoverlaywidth",
                "Block overlay width",
                ElementType.Slider(1.0, 10.0, 2.0, false)
            ))
    }

    override fun initialize() {
        register<RenderEvent.BlockHighlight> { event ->
            val block = event.blockPos.let { world?.getBlockState(it)?.block }
            if (block !in excludedBlocks) {
                event.cancel()
                renderBlock(
                    event.blockPos,
                    event.partialTicks,
                    blockoverlayfill,
                    blockoverlaycolor,
                    blockoverlaywidth.toFloat()
                )
            }
        }
    }
}