package meowing.zen.utils

import java.util.Locale
import kotlin.math.absoluteValue

object NumberUtils {
    private val suffixes = arrayOf(
        1000L to "k",
        1000000L to "m",
        1000000000L to "b",
        1000000000000L to "t",
        1000000000000000L to "p",
        1000000000000000000L to "e"
    )

    fun Number.abbreviateNumber(): String {
        val num = this.toDouble().absoluteValue
        val sign = if (this.toDouble() < 0) "-" else ""

        val (divisor, suffix) = when {
            num >= 1_000_000_000_000 -> 1_000_000_000_000.0 to "T"
            num >= 1_000_000_000 -> 1_000_000_000.0 to "B"
            num >= 1_000_000 -> 1_000_000.0 to "M"
            num >= 1_000 -> 1_000.0 to "k"
            else -> return sign + "%.0f".format(Locale.US, num)
        }

        val value = num / divisor
        val formatted = if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            val decimal = "%.1f".format(Locale.US, value)
            if (decimal.endsWith(".0")) decimal.dropLast(2) else decimal
        }
        return sign + formatted + suffix
    }

    fun Number.formatNumber(): String {
        return "%,.0f".format(Locale.US, this.toDouble())
    }

    fun Long.toFormattedDuration(short: Boolean = false): String {
        val seconds = this / 1000
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        if (short) {
            return when {
                days > 0 -> "${days}d"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "${remainingSeconds}s"
            }
        }

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            if (remainingSeconds > 0) append("${remainingSeconds}s")
        }.trimEnd()
    }

    fun Number.format(): String {
        val longValue = this.toLong()

        when {
            longValue == Long.MIN_VALUE -> return (Long.MIN_VALUE + 1).format()
            longValue < 0L -> return "-${(-longValue).format()}"
            longValue < 1000L -> return longValue.toString()
        }

        val (threshold, suffix) = suffixes.findLast { longValue >= it.first } ?: return longValue.toString()
        val scaled = longValue * 10 / threshold

        return if (scaled < 100 && scaled % 10 != 0L) "${scaled / 10.0}$suffix" else "${scaled / 10}$suffix"
    }
}