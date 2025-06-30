package meowing.zen.config.ui.types

import java.awt.Color

sealed class ConfigValue<T> {
    abstract val value: T
    abstract fun validate(input: Any?): T?
    abstract fun serialize(): Any

    data class BooleanValue(override val value: Boolean) : ConfigValue<Boolean>() {
        override fun validate(input: Any?) = input as? Boolean
        override fun serialize() = value
    }

    data class IntValue(override val value: Int, val min: Int = Int.MIN_VALUE, val max: Int = Int.MAX_VALUE) : ConfigValue<Int>() {
        override fun validate(input: Any?) = when (input) {
            is Int -> input.coerceIn(min, max)
            is Double -> input.toInt().coerceIn(min, max)
            else -> null
        }
        override fun serialize() = value
    }

    data class DoubleValue(
        override val value: Double,
        val min: Double = Double.NEGATIVE_INFINITY,
        val max: Double = Double.POSITIVE_INFINITY
    ) : ConfigValue<Double>() {
        override fun validate(input: Any?) = when (input) {
            is Double -> input.coerceIn(min, max)
            is Int -> input.toDouble().coerceIn(min, max)
            is Float -> input.toDouble().coerceIn(min, max)
            else -> null
        }
        override fun serialize() = value
    }

    data class StringValue(override val value: String, val maxLength: Int = Int.MAX_VALUE) : ConfigValue<String>() {
        override fun validate(input: Any?) = (input as? String)?.take(maxLength)
        override fun serialize() = value
    }

    data class ColorValue(override val value: Color) : ConfigValue<Color>() {
        override fun validate(input: Any?) = when (input) {
            is Color -> input
            is List<*> -> {
                val values = input.mapNotNull { (it as? Number)?.toInt() }
                if (values.size >= 4) Color(values[0], values[1], values[2], values[3]) else null
            }
            else -> null
        }
        override fun serialize() = listOf(value.red, value.green, value.blue, value.alpha)
    }
}