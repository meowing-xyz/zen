package meowing.zen.feats

import meowing.zen.Zen
import meowing.zen.Zen.Companion.config
import meowing.zen.config.ui.ConfigUI
import meowing.zen.events.Event
import meowing.zen.events.EventBus
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.LocationUtils

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
open class Feature(
    private val configKey: String? = null,
    private val variable: () -> Boolean = { true },
    area: Any? = null,
    subarea: Any? = null
) {
    val events = mutableListOf<EventBus.EventCall>()
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
        initialize()
        configKey?.let {
            Zen.registerListener(it, this)
        }
        Zen.addFeature(this)
        update()
    }

    private fun INTERNAL_isEnabled(): Boolean {
        return try {
            val configEnabled = configKey?.let {
                config.getValue(it, false)
            } ?: true
            configEnabled && variable()
        } catch (_: Exception) {
            variable()
        }
    }

    protected val mc = Zen.mc
    protected val player get() = mc.thePlayer
    protected val world get() = mc.theWorld

    open fun initialize() {}

    open fun onRegister() {
        if (Debug.debugmode) ChatUtils.addMessage("§c[Zen-Dev] §fRegistering §b$configKey")
    }

    open fun onUnregister() {
        if (Debug.debugmode) ChatUtils.addMessage("§c[Zen-Dev] §fUnregistering §b$configKey")
    }

    open fun addConfig(configUI: ConfigUI): ConfigUI = configUI

    fun isEnabled(): Boolean = INTERNAL_isEnabled() && inArea() && inSubarea()

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

    inline fun <reified T : Event> register(noinline cb: (T) -> Unit) {
        events.add(EventBus.register<T>(cb, false))
    }
}