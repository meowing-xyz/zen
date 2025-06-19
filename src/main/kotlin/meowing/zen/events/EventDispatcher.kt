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
    private val servertickevent = ServerTickEvent()
    private var entitymetadata: EntityMetadataUpdateEvent? = null
    private var chatevent: ChatReceiveEvent? = null
    private var scoreboard: ScoreboardEvent? = null
    private var tablistevent: TablistEvent? = null

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Received) {
        when (val packet = event.packet) {
            is S32PacketConfirmTransaction -> {
                if (packet.func_148888_e() || packet.actionNumber > 0) return
                MinecraftForge.EVENT_BUS.post(servertickevent)
            }
            is S1CPacketEntityMetadata -> {
                if (entitymetadata == null) entitymetadata = EntityMetadataUpdateEvent(packet)
                else entitymetadata!!.packet = packet
                MinecraftForge.EVENT_BUS.post(entitymetadata!!)
            }
            is S02PacketChat -> {
                if (chatevent == null) chatevent = ChatReceiveEvent(packet)
                else chatevent!!.packet = packet
                MinecraftForge.EVENT_BUS.post(chatevent!!)
            }
            is S3EPacketTeams, is S3CPacketUpdateScore, is S3DPacketDisplayScoreboard -> {
                if (scoreboard == null) scoreboard = ScoreboardEvent(packet)
                else scoreboard!!.packet = packet
                MinecraftForge.EVENT_BUS.post(scoreboard!!)
            }
            is S38PacketPlayerListItem -> {
                if (packet.action == S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME || packet.action == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                    if (tablistevent == null) tablistevent = TablistEvent(packet)
                    else tablistevent!!.packet = packet
                    MinecraftForge.EVENT_BUS.post(tablistevent!!)
                }
            }
        }
    }
}