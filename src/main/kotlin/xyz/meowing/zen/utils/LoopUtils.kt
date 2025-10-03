package xyz.meowing.zen.utils

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentHashMap

object LoopUtils {
    private val timerExecutor = Executors.newScheduledThreadPool(1)
    private val tasks = ConcurrentHashMap<String, ScheduledFuture<*>>()

    fun setTimeout(delay: Long, callback: Runnable) = timerExecutor.schedule(callback, delay, TimeUnit.MILLISECONDS)

    fun loop(delay: Long, stop: () -> Boolean = { false }, func: () -> Unit): String {
        val id = "${System.nanoTime()}"
        scheduleLoop(id, delay, stop, func)
        return id
    }

    fun loop(delay: () -> Number, stop: () -> Boolean = { false }, func: () -> Unit): String {
        val id = "${System.nanoTime()}"
        scheduleLoop(id, delay().toLong(), stop, func)
        return id
    }

    fun loopDynamic(delay: () -> Long, stop: () -> Boolean = { false }, func: () -> Unit): String {
        val id = "${System.nanoTime()}"
        scheduleDynamicLoop(id, delay, stop, func)
        return id
    }

    private fun scheduleDynamicLoop(id: String, delay: () -> Long, stop: () -> Boolean, func: () -> Unit) {
        fun scheduleNext() {
            if (!stop()) {
                val task = Runnable {
                    try {
                        func()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    scheduleNext()
                }
                tasks[id] = timerExecutor.schedule(task, delay(), TimeUnit.MILLISECONDS)
            } else tasks.remove(id)
        }
        scheduleNext()
    }

    private fun scheduleLoop(id: String, delay: () -> Long, stop: () -> Boolean, func: () -> Unit) {
        val task = Runnable {
            try {
                func()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (!stop())
                tasks[id] = timerExecutor.schedule({
                    scheduleLoop(id, delay, stop, func)
                }, delay(), TimeUnit.MILLISECONDS)
            else tasks.remove(id)
        }
        tasks[id] = timerExecutor.schedule(task, 0, TimeUnit.MILLISECONDS)
    }

    fun removeLoop(id: String) = tasks.remove(id)?.cancel(false) ?: false

    private fun scheduleLoop(id: String, delay: Long, stop: () -> Boolean, func: () -> Unit) = scheduleLoop(id, { delay }, stop, func)
}