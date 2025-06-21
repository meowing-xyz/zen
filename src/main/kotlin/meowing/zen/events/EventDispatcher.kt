package meowing.zen.events

import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3CPacketUpdateScore
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard
import net.minecraft.network.play.server.S3EPacketTeams

object EventDispatcher {
    init {
        EventBus.register<PacketEvent.Received> ({ event ->
            onPacketReceived(event)
        })

        EventBus.register<PacketEvent.Sent> ({ event ->
            onPacketSent(event)
        })
    }

    private fun onPacketReceived(event: PacketEvent.Received) {
        when (val packet = event.packet) {
            is S32PacketConfirmTransaction -> {
                if (packet.func_148888_e() || packet.actionNumber > 0) return
                EventBus.post(ServerTickEvent())
            }
            is S1CPacketEntityMetadata -> {
                EventBus.post(EntityMetadataUpdateEvent(packet))
            }
            is S02PacketChat -> {
                EventBus.post(ChatPacketEvent(packet))
            }
            is S3EPacketTeams, is S3CPacketUpdateScore, is S3DPacketDisplayScoreboard -> {
                EventBus.post(ScoreboardEvent(packet))
            }
            is S38PacketPlayerListItem -> {
                if (packet.action == S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME || packet.action == S38PacketPlayerListItem.Action.ADD_PLAYER)
                    EventBus.post(TablistEvent(packet))
            }
        }
    }

    private fun onPacketSent(event: PacketEvent.Sent) {
        EventBus.post(PacketEvent.Sent(event.packet))
    }
}