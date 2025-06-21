package meowing.zen.feats

import meowing.zen.Zen
import meowing.zen.events.Event
import meowing.zen.events.EventBus
import meowing.zen.utils.Location
import java.lang.reflect.Field

open class Feature(
    private val configName: String? = null,
    private val variable: () -> Boolean = { true },
    private val area: String? = null,
    private val subarea: String? = null
) {
    val events = mutableListOf<EventBus.EventCall>()
    private var isRegistered = false
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

    fun update() = onToggle(isEnabled())

    @Synchronized
    open fun onToggle(state: Boolean) {
        if (!state || !inArea() || !inSubarea()) {
            if (isRegistered) {
                events.forEach { it.unregister() }
                onUnregister()
                isRegistered = false
            }
            return
        }
        if (!isRegistered) {
            events.forEach { it.register() }
            onRegister()
            isRegistered = true
        }
    }

    fun inArea(): Boolean = area?.let { Location.area?.equals(it, true) } ?: true

    fun inSubarea(): Boolean = subarea?.let { Location.subarea?.contains(it, true) } ?: true

    inline fun <reified T : Event> register(noinline cb: (T) -> Unit) {
        events.add(EventBus.register<T>(cb, false))
    }
}