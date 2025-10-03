package xyz.meowing.zen.utils

import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.TickEvent
import java.util.*

object TickUtils {
    private val clientTaskQueue = PriorityQueue<ScheduledTask>(compareBy { it.executeTick })
    private val serverTaskQueue = PriorityQueue<ScheduledTask>(compareBy { it.executeTick })
    private val activeLoops = mutableSetOf<Long>()
    private val activeDynamicLoops = mutableMapOf<Long, DynamicLoop>()
    private val activeTimers = mutableMapOf<Long, Timer>()
    private var currentClientTick = 0L
    private var currentServerTick = 0L
    private var nextTaskId = 0L

    private data class ScheduledTask(
        val executeTick: Long,
        val action: () -> Unit,
        val interval: Long = 0,
        val taskId: Long = 0
    )

    private data class DynamicLoop(
        val intervalProvider: () -> Long,
        val action: () -> Unit,
        val isServer: Boolean
    )

    data class Timer(
        val id: Long,
        var ticks: Int,
        val onTick: () -> Unit = {},
        val onComplete: () -> Unit = {}
    )

    init {
        EventBus.register<TickEvent.Client> { onClientTick() }
        EventBus.register<TickEvent.Server> { onServerTick() }
    }

    private fun onClientTick() {
        currentClientTick++
        while (clientTaskQueue.peek()?.let { currentClientTick >= it.executeTick } == true) {
            val task = clientTaskQueue.poll()!!
            task.action()

            if (task.interval > 0 && activeLoops.contains(task.taskId)) {
                clientTaskQueue.offer(task.copy(executeTick = currentClientTick + task.interval))
            }

            if (activeDynamicLoops.containsKey(task.taskId)) {
                val dynamicLoop = activeDynamicLoops[task.taskId]!!
                val nextInterval = dynamicLoop.intervalProvider()
                clientTaskQueue.offer(task.copy(executeTick = currentClientTick + nextInterval, interval = 0))
            }
        }
    }

    private fun onServerTick() {
        currentServerTick++

        while (serverTaskQueue.peek()?.let { currentServerTick >= it.executeTick } == true) {
            val task = serverTaskQueue.poll()!!
            task.action()

            if (task.interval > 0 && activeLoops.contains(task.taskId)) {
                serverTaskQueue.offer(task.copy(executeTick = currentServerTick + task.interval))
            }

            if (activeDynamicLoops.containsKey(task.taskId)) {
                val dynamicLoop = activeDynamicLoops[task.taskId]!!
                val nextInterval = dynamicLoop.intervalProvider()
                serverTaskQueue.offer(task.copy(executeTick = currentServerTick + nextInterval, interval = 0))
            }
        }

        val completedTimers = mutableListOf<Long>()
        activeTimers.values.forEach { timer ->
            if (timer.ticks > 0) {
                timer.ticks--
                timer.onTick()
            } else {
                timer.onComplete()
                completedTimers.add(timer.id)
            }
        }
        completedTimers.forEach { activeTimers.remove(it) }
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

    fun loopDynamic(intervalProvider: () -> Long, action: () -> Unit): Long {
        val taskId = nextTaskId++
        activeDynamicLoops[taskId] = DynamicLoop(intervalProvider, action, false)
        val initialInterval = intervalProvider()
        clientTaskQueue.offer(ScheduledTask(currentClientTick + initialInterval, action, 0, taskId))
        return taskId
    }

    fun loopServerDynamic(intervalProvider: () -> Long, action: () -> Unit): Long {
        val taskId = nextTaskId++
        activeDynamicLoops[taskId] = DynamicLoop(intervalProvider, action, true)
        val initialInterval = intervalProvider()
        serverTaskQueue.offer(ScheduledTask(currentServerTick + initialInterval, action, 0, taskId))
        return taskId
    }

    fun createTimer(ticks: Int, onTick: () -> Unit = {}, onComplete: () -> Unit = {}): Long {
        val timerId = nextTaskId++
        activeTimers[timerId] = Timer(timerId, ticks, onTick, onComplete)
        return timerId
    }

    fun cancelLoop(taskId: Long) {
        activeLoops.remove(taskId)
        activeDynamicLoops.remove(taskId)
    }

    fun cancelTimer(timerId: Long) {
        activeTimers.remove(timerId)
    }

    fun getTimer(timerId: Long): Timer? = activeTimers[timerId]

    fun getCurrentClientTick() = currentClientTick

    fun getCurrentServerTick() = currentServerTick
}