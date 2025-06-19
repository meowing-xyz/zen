package meowing.zen.events

import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.Event

class ChatReceiveEvent (
    var packet: S02PacketChat
) : Event()