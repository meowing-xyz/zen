package meowing.zen.events

import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

abstract class PacketEvent: Event() {
    @Cancelable
    class Received(val packet: Packet<*>): PacketEvent()

    @Cancelable
    class Sent(val packet: Packet<*>): PacketEvent()
}