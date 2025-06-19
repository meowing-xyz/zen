package meowing.zen.utils

import meowing.zen.events.ServerTickEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*

object TickScheduler {
    private val clientTaskQueue = PriorityQueue<ScheduledTask>(compareBy { it.executeTick })
    private val serverTaskQueue = PriorityQueue<ScheduledTask>(compareBy { it.executeTick })
    private var currentClientTick = 0L
    private var currentServerTick = 0L
    private var nextTaskId = 0L

    private data class ScheduledTask(
        val executeTick: Long,
        val action: () -> Unit,
        val interval: Long = 0,
        val taskId: Long = 0
    )

    private val activeLoops = mutableSetOf<Long>()

    fun register() {
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun schedule(delayTicks: Long, action: () -> Unit) {
        clientTaskQueue.offer(ScheduledTask(currentClientTick + delayTicks, action))
    }

    fun scheduleServer(delayTicks: Long, action: () -> Unit) {
        serverTaskQueue.offer(ScheduledTask(currentServerTick + delayTicks, action))
    }

    fun loop(intervalTicks: Long, action: () -> Unit): Long {
        val taskId = nextTaskId++
        activeLoops.add(taskId)
        clientTaskQueue.offer(ScheduledTask(currentClientTick + intervalTicks, action, intervalTicks, taskId))
        return taskId
    }

    fun loopServer(intervalTicks: Long, action: () -> Unit): Long {
        val taskId = nextTaskId++
        activeLoops.add(taskId)
        serverTaskQueue.offer(ScheduledTask(currentServerTick + intervalTicks, action, intervalTicks, taskId))
        return taskId
    }

    fun cancelLoop(taskId: Long) {
        activeLoops.remove(taskId)
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        currentClientTick++
        while (clientTaskQueue.peek()?.let { currentClientTick >= it.executeTick } == true) {
            val task = clientTaskQueue.poll()!!
            task.action()
            if (task.interval > 0 && activeLoops.contains(task.taskId))
                clientTaskQueue.offer(task.copy(executeTick = currentClientTick + task.interval))
        }
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        currentServerTick++
        while (serverTaskQueue.peek()?.let { currentServerTick >= it.executeTick } == true) {
            val task = serverTaskQueue.poll()!!
            task.action()
            if (task.interval > 0 && activeLoops.contains(task.taskId))
                serverTaskQueue.offer(task.copy(executeTick = currentServerTick + task.interval))
        }
    }

    fun getCurrentClientTick() = currentClientTick
    fun getCurrentServerTick() = currentServerTick
}