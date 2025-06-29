package meowing.zen.feats

import meowing.zen.Zen
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

    fun isEnabled(): Boolean {
        return try {
            val configEnabled = configKey?.let {
                Zen.config.getValue(it, false)
            } ?: true
            configEnabled && variable()
        } catch (_: Exception) {
            variable()
        }
    }

    fun update() = onToggle(isEnabled() && inArea() && inSubarea())

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

    fun inArea(): Boolean = areaLower?.let { LocationUtils.area == it } ?: true

    fun inSubarea(): Boolean = subareaLower?.let { LocationUtils.subarea?.contains(it) == true } ?: true

    inline fun <reified T : Event> register(noinline cb: (T) -> Unit) {
        events.add(EventBus.register<T>(cb, false))
    }
}