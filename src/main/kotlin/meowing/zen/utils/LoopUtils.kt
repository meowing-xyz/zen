package meowing.zen.utils

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// Code snippet from https://github.com/Noamm9/NoammAddons/blob/master/src/main/kotlin/noammaddons/utils/ThreadUtils.kt
object LoopUtils {
    private val timerExecutor = Executors.newScheduledThreadPool(1)

    fun loop(delay: Long, stop: () -> Boolean = { false }, func: () -> Unit) {
        val task = object : Runnable {
            override fun run() {
                try {
                    func()
                } catch (e: Exception) {
                    System.err.println("[Zen] Exeception in loop: $e")
                    e.printStackTrace()
                } finally {
                    if (!stop()) timerExecutor.schedule(this, delay, TimeUnit.MILLISECONDS)
                }
            }
        }
        timerExecutor.execute(task)
    }

    fun loop(delay: () -> Number, stop: () -> Boolean = { false }, func: () -> Unit) {
        val task = object : Runnable {
            override fun run() {
                try {
                    func()
                } catch (e: Exception) {
                    System.err.println("[Zen] Exeception in loop: $e")
                    e.printStackTrace()
                } finally {
                    if (!stop()) timerExecutor.schedule(this, delay().toLong(), TimeUnit.MILLISECONDS)
                }
            }
        }
        timerExecutor.execute(task)
    }
}
