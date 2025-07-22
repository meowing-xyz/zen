package meowing.zen.feats.rift

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.PacketEvent
import meowing.zen.events.RenderEvent
import meowing.zen.feats.Feature
import meowing.zen.utils.Render3D
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import java.awt.Color
import kotlin.math.hypot

@Zen.Module
object BerberisHelper : Feature("berberishelper", area = "the rift", subarea = "dreadfarm") {
    private var blockPos: BlockPos? = null

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Rift", "Berberis Helper", ConfigElement(
                "berberishelper",
                "Berberis highlight",
                "Boxes the correct berberis plant.",
                ElementType.Switch(false)
            ))
            .addElement("Rift", "Berberis Helper", ConfigElement(
                "berberishelpercolor",
                "Colorpicker",
                "Color for berberis highlight.",
                ElementType.ColorPicker(Color(0, 255, 255, 127))
            ))
    }

    override fun initialize() {
        register<PacketEvent.Received> { event ->
            val packet = event.packet as? S2APacketParticles ?: return@register
            if (packet.particleType.particleID != EnumParticleTypes.FIREWORKS_SPARK.particleID) return@register
            if (hypot(player.posX - packet.xCoordinate, player.posZ - packet.zCoordinate) > 20) return@register

            val pos = BlockPos(packet.xCoordinate - 1, packet.yCoordinate, packet.zCoordinate - 1)
            val below = BlockPos(packet.xCoordinate - 1, packet.yCoordinate - 1, packet.zCoordinate - 1)

            if (world.getBlockState(pos).block == Blocks.deadbush && world.getBlockState(below).block == Blocks.farmland) blockPos = pos
        }

        register<RenderEvent.World> { event ->
            blockPos?.let {
                Render3D.renderBlock(it, event.partialTicks, true, config.berberishelpercolor, 2.0f)
            }
        }
    }
}