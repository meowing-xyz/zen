package meowing.zen.config.ui.types

import java.awt.Color

sealed class ConfigValue<T>(open val value: T) {
    abstract fun validate(input: Any?): T?
    abstract fun serialize(): Any

    class BooleanValue(override val value: Boolean) : ConfigValue<Boolean>(value) {
        override fun validate(input: Any?) = input as? Boolean
        override fun serialize() = value
    }

    class IntValue(
        override val value: Int,
        private val min: Int = Int.MIN_VALUE,
        private val max: Int = Int.MAX_VALUE
    ) : ConfigValue<Int>(value) {
        override fun validate(input: Any?) = when (input) {
            is Int -> input.coerceIn(min, max)
            else -> (input as? Number)?.toInt()?.coerceIn(min, max)
        }
        override fun serialize() = value
    }

    class DoubleValue(
        override val value: Double,
        private val min: Double = Double.NEGATIVE_INFINITY,
        private val max: Double = Double.POSITIVE_INFINITY
    ) : ConfigValue<Double>(value) {
        override fun validate(input: Any?) = (input as? Number)?.toDouble()?.coerceIn(min, max)
        override fun serialize() = value
    }

    class StringValue(
        override val value: String,
        private val maxLength: Int = Int.MAX_VALUE
    ) : ConfigValue<String>(value) {
        override fun validate(input: Any?) = (input as? String)?.take(maxLength)
        override fun serialize() = value
    }

    class ColorValue(override val value: Color) : ConfigValue<Color>(value) {
        override fun validate(input: Any?) = when (input) {
            is Color -> input
            is Map<*, *> -> input.toColor()
            is List<*> -> input.takeIf { it.size >= 4 }?.let {
                Color(
                    (it[0] as? Number)?.toInt() ?: 255,
                    (it[1] as? Number)?.toInt() ?: 255,
                    (it[2] as? Number)?.toInt() ?: 255,
                    (it[3] as? Number)?.toInt() ?: 255
                )
            }
            is Number -> Color(input.toInt(), true)
            else -> null
        }

        override fun serialize() = mapOf(
            "r" to value.red,
            "g" to value.green,
            "b" to value.blue,
            "a" to value.alpha
        )
    }
}

private fun Map<*, *>.toColor(): Color {
    return Color(
        (this["r"] as? Number)?.toInt() ?: 255,
        (this["g"] as? Number)?.toInt() ?: 255,
        (this["b"] as? Number)?.toInt() ?: 255,
        (this["a"] as? Number)?.toInt() ?: 255
    )
}