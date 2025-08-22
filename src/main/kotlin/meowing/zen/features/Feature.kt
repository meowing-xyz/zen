package meowing.zen.features

import meowing.zen.Zen
import meowing.zen.Zen.Companion.configUI
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.events.Event
import meowing.zen.events.EventBus
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.LocationUtils
import meowing.zen.utils.LoopUtils
import meowing.zen.utils.TickUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.multiplayer.WorldClient
import org.lwjgl.input.Mouse

//TODO: Add inSkyblock check
/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
open class Feature(
    val configKey: String? = null,
    area: Any? = null,
    subarea: Any? = null
) {
    val events = mutableListOf<EventBus.EventCall>()
    val tickLoopIds = mutableSetOf<Long>()
    val timerLoopIds = mutableSetOf<String>()
    val tickTimerIds = mutableSetOf<Long>()
    val namedEventCalls = mutableMapOf<String, EventBus.EventCall>()
    private var setupLoops: (() -> Unit)? = null
    private var isRegistered = false
    private val areas = when (area) {
        is String -> listOf(area.lowercase())
        is List<*> -> area.filterIsInstance<String>().map { it.lowercase() }
        else -> emptyList()
    }
    private val subareas = when (subarea) {
        is String -> listOf(subarea.lowercase())
        is List<*> -> subarea.filterIsInstance<String>().map { it.lowercase() }
        else -> emptyList()
    }

    init {
        Zen.addFeature(this)
    }

    private fun checkConfig(): Boolean {
        return try {
            val configEnabled = configKey?.let {
                configUI.getConfigValue(it) as? Boolean ?: false
            } ?: true
            configEnabled
        } catch (_: Exception) {
            false
        }
    }

    protected val mc: Minecraft = Zen.mc
    protected val fontRenderer: FontRenderer = mc.fontRendererObj
    protected inline val sr get() = ScaledResolution(mc)
    protected inline val mouseX get() = (Mouse.getX() * sr.scaledWidth / mc.displayWidth).toFloat()
    protected inline val mouseY get() = (sr.scaledHeight - Mouse.getY() * sr.scaledHeight / mc.displayHeight).toFloat()
    protected inline val player: EntityPlayerSP? get() = mc.thePlayer
    protected inline val world: WorldClient? get() = mc.theWorld

    open fun initialize() {}

    protected fun setupLoops(block: () -> Unit) {
        setupLoops = block
    }

    open fun onRegister() {
        if (Debug.debugmode) ChatUtils.addMessage("$prefix §fRegistering §b$configKey")
        setupLoops?.invoke()
    }

    open fun onUnregister() {
        if (Debug.debugmode) ChatUtils.addMessage("$prefix §fUnregistering §b$configKey")
        cancelLoops()
    }

    open fun addConfig(configUI: ConfigUI): ConfigUI = configUI

    fun isEnabled(): Boolean = checkConfig() && inArea() && inSubarea()

    fun update() = onToggle(isEnabled())

    @Synchronized
    open fun onToggle(state: Boolean) {
        if (state == isRegistered) return

        if (state) {
            events.forEach { it.register() }
            onRegister()
            isRegistered = true
        } else {
            events.forEach { it.unregister() }
            onUnregister()
            isRegistered = false
        }
    }

    fun inArea(): Boolean = areas.isEmpty() || areas.any { LocationUtils.checkArea(it) }

    fun inSubarea(): Boolean = subareas.isEmpty() || subareas.any { LocationUtils.checkSubarea(it) }

    inline fun <reified T : Event> register(priority: Int = 0, noinline cb: (T) -> Unit) {
        events.add(EventBus.register<T>(priority, cb, false))
    }

    inline fun <reified T : Event> createCustomEvent(name: String, priority: Int = 0, noinline cb: (T) -> Unit) {
        val eventCall = EventBus.register<T>(priority, cb, false)
        namedEventCalls[name] = eventCall
    }

    fun registerEvent(name: String) {
        namedEventCalls[name]?.register()
    }

    fun unregisterEvent(name: String) {
        namedEventCalls[name]?.unregister()
    }

    inline fun <reified T> loop(intervalTicks: Long, noinline action: () -> Unit): Any {
        return when (T::class) {
            ClientTick::class -> {
                val id = TickUtils.loop(intervalTicks, action)
                tickLoopIds.add(id)
                id
            }
            ServerTick::class -> {
                val id = TickUtils.loopServer(intervalTicks, action)
                tickLoopIds.add(id)
                id
            }
            Timer::class -> {
                val id = LoopUtils.loop(intervalTicks, { false }, action)
                timerLoopIds.add(id)
                id
            }
            else -> throw IllegalArgumentException("Unsupported loop type: ${T::class}")
        }
    }

    inline fun <reified T> loopDynamic(noinline delay: () -> Long, noinline stop: () -> Boolean = { false }, noinline action: () -> Unit): Any {
        return when (T::class) {
            Timer::class -> {
                val id = LoopUtils.loopDynamic(delay, stop, action)
                timerLoopIds.add(id)
                id
            }
            ClientTick::class -> {
                val id = TickUtils.loopDynamic(delay, action)
                tickLoopIds.add(id)
                id
            }
            ServerTick::class -> {
                val id = TickUtils.loopServerDynamic(delay, action)
                tickLoopIds.add(id)
                id
            }
            else -> throw IllegalArgumentException("Unsupported loop type: ${T::class}")
        }
    }

    fun createTimer(ticks: Int, onTick: () -> Unit = {}, onComplete: () -> Unit = {}): Long {
        val id = TickUtils.createTimer(ticks, onTick, onComplete)
        tickTimerIds.add(id)
        return id
    }

    fun getTimer(timerId: Long): TickUtils.Timer? = TickUtils.getTimer(timerId)

    private fun cancelLoops() {
        tickLoopIds.forEach {
            TickUtils.cancelLoop(it)
        }
        timerLoopIds.forEach {
            LoopUtils.removeLoop(it)
        }
        tickTimerIds.forEach {
            TickUtils.cancelTimer(it)
        }
        tickLoopIds.clear()
        timerLoopIds.clear()
        tickTimerIds.clear()
    }

    fun hasAreas(): Boolean = areas.isNotEmpty()
    fun hasSubareas(): Boolean = subareas.isNotEmpty()
}

class ClientTick
class ServerTick
class Timer