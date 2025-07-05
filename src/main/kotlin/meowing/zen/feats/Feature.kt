package meowing.zen.feats

import meowing.zen.Zen
import meowing.zen.config.ui.ConfigUI
import meowing.zen.events.Event
import meowing.zen.events.EventBus
import meowing.zen.utils.LocationUtils

open class Feature(
    private val configKey: String? = null,
    private val variable: () -> Boolean = { true },
    area: String? = null,
    subarea: String? = null
) {
    val events = mutableListOf<EventBus.EventCall>()
    private var isRegistered = false
    private val areaLower = area?.lowercase()
    private val subareaLower = subarea?.lowercase()

    init {
        initialize()
        configKey?.let {
            Zen.registerListener(it, this)
        }
        Zen.addFeature(this)
        update()
    }

    open fun initialize() {}

    open fun onRegister() {}

    open fun onUnregister() {}

    open fun addConfig(configUI: ConfigUI): ConfigUI = configUI

    fun INTERNAL_isEnabled(): Boolean {
        return try {
            val configEnabled = configKey?.let {
                Zen.config.getValue(it, false)
            } ?: true
            configEnabled && variable()
        } catch (_: Exception) {
            variable()
        }
    }

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

    fun inArea(): Boolean = LocationUtils.checkArea(areaLower)

    fun inSubarea(): Boolean = LocationUtils.checkSubarea(subareaLower)

    inline fun <reified T : Event> register(noinline cb: (T) -> Unit) {
        events.add(EventBus.register<T>(cb, false))
    }
}