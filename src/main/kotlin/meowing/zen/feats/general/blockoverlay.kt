package meowing.zen.feats.general

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.BlockHighlightEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.RenderUtils.renderBlock
import net.minecraft.init.Blocks

object blockoverlay : Feature("blockoverlay") {
    private val excludedBlocks = setOf(Blocks.air, Blocks.flowing_lava, Blocks.lava, Blocks.flowing_water, Blocks.water)

    override fun initialize() {
        register<BlockHighlightEvent> { event ->
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