package meowing.zen.events

import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Event

class ScoreboardEvent(var packet: Packet<*>) : Event()