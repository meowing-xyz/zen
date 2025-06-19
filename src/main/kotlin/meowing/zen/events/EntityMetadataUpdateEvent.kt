package meowing.zen.events


import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraftforge.fml.common.eventhandler.Event

class EntityMetadataUpdateEvent(
    var packet: S1CPacketEntityMetadata
) : Event()