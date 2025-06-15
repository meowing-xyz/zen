package meowing.zen.utils

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*

object TickScheduler {
    private val taskQueue = PriorityQueue<ScheduledTask>(compareBy { it.executeTick })
    private var currentTick = 0L

    private data class ScheduledTask(
        val executeTick: Long,
        val action: () -> Unit
    )

    fun schedule(delayTicks: Long, action: () -> Unit) {
        taskQueue.offer(ScheduledTask(currentTick + delayTicks, action))
    }

    fun register() {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return

        currentTick++

        while (taskQueue.peek()?.let { currentTick >= it.executeTick } == true) {
            taskQueue.poll().action()
        }
    }
}