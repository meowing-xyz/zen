package meowing.zen.config

import meowing.zen.Zen.Companion.configUI
import kotlin.reflect.KProperty

class ConfigDelegate<T>(key: String) {
    private var cachedValue: T

    init {
        @Suppress("UNCHECKED_CAST")
        cachedValue = (configUI.getConfigValue(key) ?: configUI.getDefaultValue(key)) as T

        configUI.registerListener(key) { newValue ->
            @Suppress("UNCHECKED_CAST")
            cachedValue = newValue as T
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = cachedValue

    // TODO: Add functionality maybe?
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {}
}