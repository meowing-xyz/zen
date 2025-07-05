package meowing.zen.config.ui.core

import meowing.zen.config.ui.types.ConfigValue

class ConfigValidator {
    private val validators = mutableMapOf<String, ConfigValue<*>>()

    fun register(key: String, validator: ConfigValue<*>) {
        validators[key] = validator
    }

    fun validate(key: String, value: Any?): Any? = validators[key]?.validate(value) ?: value

    fun getDefault(key: String): Any? = validators[key]?.value
}