package meowing.zen.events

import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraftforge.fml.common.eventhandler.Event

class TablistEvent(val packet: S38PacketPlayerListItem) : Event()