package meowing.zen.utils

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*

object TickScheduler {
    private val clientTaskQueue = PriorityQueue<ScheduledTask>(compareBy { it.executeTick })
    private val serverTaskQueue = PriorityQueue<ScheduledTask>(compareBy { it.executeTick })
    private var currentClientTick = 0L
    private var currentServerTick = 0L

    private data class ScheduledTask(
        val executeTick: Long,
        val action: () -> Unit
    )

    fun register() {
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun schedule(delayTicks: Long, action: () -> Unit) {
        clientTaskQueue.offer(ScheduledTask(currentClientTick + delayTicks, action))
    }

    fun scheduleServer(delayTicks: Long, action: () -> Unit) {
        serverTaskQueue.offer(ScheduledTask(currentServerTick + delayTicks, action))
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        currentClientTick++
        while (clientTaskQueue.peek()?.let { currentClientTick >= it.executeTick } == true) clientTaskQueue.poll().action()
    }

    fun onServerTick() {
        currentServerTick++
        while (serverTaskQueue.peek()?.let { currentServerTick >= it.executeTick } == true) serverTaskQueue.poll().action()
    }
}