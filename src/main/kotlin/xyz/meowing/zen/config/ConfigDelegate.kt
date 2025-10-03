package xyz.meowing.zen.config
import xyz.meowing.zen.Zen.Companion.configUI
import xyz.meowing.zen.config.ui.elements.MCColorCode
import java.awt.Color
import kotlin.reflect.KProperty

inline fun <reified T> ConfigDelegate(key: String) = Handler(key, T::class.java)

class Handler<T>(private val key: String, private val clazz: Class<T>) {
    private var cachedValue: T
    private var isInitialized = false

    init {
        @Suppress("UNCHECKED_CAST")
        cachedValue = getBuiltInDefault() as T
        configUI.registerListener(key) { newValue ->
            @Suppress("UNCHECKED_CAST")
            cachedValue = convertValue(newValue) as T
            isInitialized = true
        }
    }

    private fun convertValue(value: Any?): Any {
        return when {
            clazz == MCColorCode::class.java && value is String -> MCColorCode.entries.find { it.code == value || it.name == value || it.displayName == value } ?: getBuiltInDefault()
            clazz == Set::class.java && value is List<*> -> value.mapNotNull { (it as? Number)?.toInt() }.toSet()
            else -> value ?: getBuiltInDefault()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getBuiltInDefault(): Any = when (clazz) {
        String::class.java -> ""
        MCColorCode::class.java -> MCColorCode.WHITE
        Int::class.java, Integer::class.java -> 0
        Long::class.java, java.lang.Long::class.java -> 0L
        Float::class.java, java.lang.Float::class.java -> 0f
        Double::class.java, java.lang.Double::class.java -> 0.0
        Boolean::class.java, java.lang.Boolean::class.java -> false
        Color::class.java -> Color(0, 255, 255, 127)
        List::class.java -> emptyList<Any>()
        Set::class.java -> emptySet<Any>()
        Map::class.java -> emptyMap<Any, Any>()
        else -> throw IllegalArgumentException("Unsupported type: $clazz")
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!isInitialized) {
            configUI.getConfigValue(key)?.let { currentValue ->
                @Suppress("UNCHECKED_CAST")
                cachedValue = convertValue(currentValue) as T
                isInitialized = true
            }
        }
        return cachedValue
    }

    // TODO: Add functionality maybe?
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {}
}