package meowing.zen.feats

import meowing.zen.Zen
import meowing.zen.events.Event
import meowing.zen.events.EventBus
import meowing.zen.utils.LocationUtils
import java.lang.reflect.Field

open class Feature(
    private val configName: String? = null,
    private val variable: () -> Boolean = { true },
    area: String? = null,
    subarea: String? = null
) {
    val events = mutableListOf<EventBus.EventCall>()
    private var isRegistered = false
    private val areaLower = area?.lowercase()
    private val subareaLower = subarea?.lowercase()
    private val configField: Field? by lazy {
        configName?.let {
            try {
                Zen.config::class.java.getDeclaredField(it).apply { isAccessible = true }
            } catch (_: Exception) {
                println("[Zen] Config field $it not found")
                null
            }
        }
    }

    init {
        configField?.let { field ->
            if (field.get(Zen.config) == null) field.set(Zen.config, false)
        }
        initialize()
        configName?.let { Zen.registerListener(it, this) }
        Zen.addFeature(this)
        update()
    }

    open fun initialize() {}

    open fun onRegister() {}

    open fun onUnregister() {}

    fun isEnabled(): Boolean {
        return try {
            val configEnabled = configField?.get(Zen.config) as? Boolean ?: true
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