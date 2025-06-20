package meowing.zen.events

import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3CPacketUpdateScore
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EventDispatcher {
    @SubscribeEvent
    fun onPacket(event: PacketEvent.Received) {
        when (val packet = event.packet) {
            is S32PacketConfirmTransaction -> {
                if (packet.func_148888_e() || packet.actionNumber > 0) return
                MinecraftForge.EVENT_BUS.post(ServerTickEvent())
            }
            is S1CPacketEntityMetadata -> {
                MinecraftForge.EVENT_BUS.post(EntityMetadataUpdateEvent(packet))
            }
            is S02PacketChat -> {
                MinecraftForge.EVENT_BUS.post(ChatReceiveEvent(packet))
            }
            is S3EPacketTeams, is S3CPacketUpdateScore, is S3DPacketDisplayScoreboard -> {
                MinecraftForge.EVENT_BUS.post(ScoreboardEvent(packet))
            }
            is S38PacketPlayerListItem -> {
                if (packet.action == S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME || packet.action == S38PacketPlayerListItem.Action.ADD_PLAYER)
                    MinecraftForge.EVENT_BUS.post(TablistEvent(packet))
            }
        }
    }
}